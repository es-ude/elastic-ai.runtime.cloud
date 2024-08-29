package org.ude.es.ota;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.ude.es.Monitor;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static org.ude.es.Monitor.monitorCommunicationEndpoint;

@Controller
@RequestMapping({ "/app" })
public class AppController {

    public static HashMap<String, byte[]> applications = new HashMap<>();
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static String receivedByClient = "NULL";
    public static volatile boolean statusIsUpdated;
    public static ResponseEntity<?> response;
    static CountDownLatch latch;

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> getHexAppChunk(
        @PathVariable String name,
        @RequestParam Integer chunkNumber,
        @RequestParam(defaultValue = "1024") Integer chunkMaxSize
    ) {
        if (!applications.containsKey(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] hexFile = applications.get(name);
        int start = chunkNumber * chunkMaxSize;
        int end = chunkNumber * chunkMaxSize + chunkMaxSize;

        if (chunkNumber * chunkMaxSize > hexFile.length) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (end > hexFile.length) {
            end = hexFile.length;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(
            HttpHeaders.CONTENT_TYPE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE
        );
        httpHeaders.set(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename("fragment.hex")
                .build()
                .toString()
        );
        return ResponseEntity.ok()
            .headers(httpHeaders)
            .body(Arrays.copyOfRange(hexFile, start, end));
    }

    public static void uploadAPP(
        String clientID,
        int size,
        String name,
        int appID
    ) {
        RemoteCommunicationEndpoint clientStub =
            new RemoteCommunicationEndpoint(clientID);
        clientStub.bindToCommunicationEndpoint(
            monitorCommunicationEndpoint.getBroker()
        );
        clientStub.publishCommand(
            "FlashImage",
            String.format(
                "URL:http://%s:%s/app/;NAME:%s;SIZE:%d;POSITION:%d",
                Monitor.HOST_IP,
                Monitor.PORT,
                name,
                size,
                    appID
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

    @PostMapping("/reboot")
    public ResponseEntity<?> handleReboot(
            @RequestParam("clientID") String clientID,
            @RequestParam("sector") String sector) {

        RemoteCommunicationEndpoint clientStub =
                new RemoteCommunicationEndpoint(clientID);
        clientStub.bindToCommunicationEndpoint(
                monitorCommunicationEndpoint.getBroker()
        );
        clientStub.publishCommand("FlashImage", sector);

        return  response = ResponseEntity.status(200).build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("clientID") String clientID,
        @RequestParam("sector") int sector
    ) throws IOException, InterruptedException {
        String fileName = Objects.requireNonNull(
            file.getOriginalFilename()
        ).split("\\.")[0];

        AppController.applications.put(fileName, file.getBytes());

        System.out.println(
            "Application uploaded: " + ANSI_GREEN + fileName + ANSI_RESET
        );

        try {
            uploadAPP(
                clientID,
                file.getBytes().length,
                fileName,
                    sector
            );
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
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
