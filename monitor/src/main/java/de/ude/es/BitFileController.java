package de.ude.es;

import static de.ude.es.MonitoringServiceApplication.monitorCommunicationEndpoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;

@Controller
@RequestMapping({ "/bitfile" })
public class BitFileController {

    public static final int BITFILE_CHUNK_SIZE = 512;
    public static HashMap<String, byte[]> bitFiles = new HashMap<>();
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static String receivedByDevice = "NULL";
    public static volatile boolean statusIsUpdated;
    public static ResponseEntity<?> response;
    static CountDownLatch latch;

    @GetMapping("/{name}/{dataId}")
    public ResponseEntity<byte[]> demo(
        @PathVariable String name,
        @PathVariable Integer dataId
    ) {
        if (!bitFiles.containsKey(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] bitFile = bitFiles.get(name);
        int start = dataId * BITFILE_CHUNK_SIZE;
        int end = dataId * BITFILE_CHUNK_SIZE + BITFILE_CHUNK_SIZE;

        if (dataId * BITFILE_CHUNK_SIZE > bitFile.length) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (end > bitFile.length) {
            end = bitFile.length;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(
            HttpHeaders.CONTENT_TYPE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE
        );
        httpHeaders.set(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition
                .attachment()
                .filename("bitFile.bit")
                .build()
                .toString()
        );
        return ResponseEntity
            .ok()
            .headers(httpHeaders)
            .body(Arrays.copyOfRange(bitFile, start, end));
    }

    public static void uploadBitFile(String twinID, int size, String name) {
        RemoteCommunicationEndpoint deviceStub =
            new RemoteCommunicationEndpoint(twinID);
        deviceStub.bindToCommunicationEndpoint(
            monitorCommunicationEndpoint.getBrokerStub()
        );
        deviceStub.publishCommand(
            "FLASH",
            String.format(
                "URL:http://%s:8081/bitfile/%s/;SIZE:%d;",
                MonitoringServiceApplication.IP_ADDRESS,
                name,
                size
            )
        );
        statusIsUpdated = false;
        latch = new CountDownLatch(1);
        deviceStub.subscribeForDone(
            "FLASH",
            posting -> {
                System.out.println("FLASH DONE");
                deviceStub.unsubscribeFromDone("FLASH");
                receivedByDevice = posting.data();
                statusIsUpdated = true;
                latch.countDown();
            }
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("twinID") String twinID
    ) throws IOException, InterruptedException {
        String fileName = Objects
            .requireNonNull(file.getOriginalFilename())
            .split("\\.")[0];
        System.out.println(fileName);
        BitFileController.bitFiles.put(fileName, file.getBytes());

        System.out.println(
            "BitFile uploaded: " + ANSI_GREEN + fileName + ANSI_RESET
        );

        try {
            uploadBitFile(twinID, file.getBytes().length, fileName);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }

        latch.await();
        if (receivedByDevice.equals("SUCCESS")) {
            System.out.println("success");
            response = ResponseEntity.status(200).build();
        } else {
            System.out.println("failed");
            response = ResponseEntity.status(400).build();
        }

        System.out.println(response);
        return response;
    }
}