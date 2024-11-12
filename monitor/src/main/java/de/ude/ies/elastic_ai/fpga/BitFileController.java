package de.ude.ies.elastic_ai.fpga;

import static de.ude.ies.elastic_ai.Monitor.monitorCommunicationEndpoint;

import de.ude.ies.elastic_ai.Monitor;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping({ "/bitfile" })
public class BitFileController {

    public static HashMap<String, byte[]> bitFiles = new HashMap<>();
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static String receivedByClient = "NULL";
    public static volatile boolean statusIsUpdated;
    public static ResponseEntity<?> response;
    static CountDownLatch latch;

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> getBitFileChunk(
        @PathVariable String name,
        @RequestParam Integer chunkNumber,
        @RequestParam(defaultValue = "256") Integer chunkMaxSize
    ) {
        if (!bitFiles.containsKey(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] bitFile = bitFiles.get(name);
        int start = chunkNumber * chunkMaxSize;
        int end = chunkNumber * chunkMaxSize + chunkMaxSize;

        if (chunkNumber * chunkMaxSize > bitFile.length) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (end > bitFile.length) {
            end = bitFile.length;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        httpHeaders.set(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename("bitFile.bit").build().toString()
        );
        return ResponseEntity.ok()
            .headers(httpHeaders)
            .body(Arrays.copyOfRange(bitFile, start, end));
    }

    public static void uploadBitFile(String clientID, int size, String name, int startSectorID) {
        RemoteCommunicationEndpoint clientStub = new RemoteCommunicationEndpoint(clientID);
        clientStub.bindToCommunicationEndpoint(monitorCommunicationEndpoint.getBroker());
        clientStub.publishCommand(
            "FLASH",
            String.format(
                "URL:http://%s:%s/bitfile/%s/;SIZE:%d;POSITION:%d",
                Monitor.HOST_IP,
                Monitor.PORT,
                name,
                size,
                startSectorID
            )
        );
        statusIsUpdated = false;
        latch = new CountDownLatch(1);
        clientStub.subscribeForDone("FLASH", posting -> {
            System.out.println("FLASH DONE");
            clientStub.unsubscribeFromDone("FLASH");
            receivedByClient = posting.data();
            statusIsUpdated = true;
            latch.countDown();
        });
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("clientID") String clientID,
        @RequestParam("startSectorID") int startSectorID
    ) throws IOException, InterruptedException {
        String fileName = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0];
        System.out.println(fileName);
        BitFileController.bitFiles.put(fileName, file.getBytes());

        System.out.println("BitFile uploaded: " + ANSI_GREEN + fileName + ANSI_RESET);

        try {
            uploadBitFile(clientID, file.getBytes().length, fileName, startSectorID);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        latch.await();
        if (receivedByClient.equals("SUCCESS")) {
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
