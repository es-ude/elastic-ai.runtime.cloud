package de.ude.es;

import static de.ude.es.MonitoringServiceApplication.monitorCommunicationEndpoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;

@Controller
@RequestMapping({ "/getfile" })
public class BitFile {

    public static final int BITFILE_CHUNK_SIZE = 1024;
    public static HashMap<String, byte[]> bitFiles = new HashMap<>();

    @GetMapping("/{name}/{dataId}")
    public ResponseEntity<byte[]> demo(
        @PathVariable String name,
        @PathVariable Integer dataId
    ) throws IOException {
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
        RemoteCommunicationEndpoint deviceStub = new RemoteCommunicationEndpoint(twinID);
        deviceStub.bindToCommunicationEndpoint(monitorCommunicationEndpoint.getBrokerStub());
        deviceStub.publishCommand(
            "FLASH",
            String.format(
                "URL:http://%s:8081/getfile/%s/;SIZE:%d;",
                MonitoringServiceApplication.IP_ADDRESS,
                name,
                size
            )
        );
        deviceStub.subscribeForDone(
            "FLASH",
            posting -> {
                System.out.println("FLASH DONE");
                deviceStub.unsubscribeFromDone("FLASH");
            }
        );
    }
}
