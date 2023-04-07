package de.ude.es;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@Controller
@RequestMapping({"/bitFile"})
public class BitFile {

    public static final int BITFILE_CHUNK_SIZE = 1024;
    public static HashMap<String, byte[]> bitFiles = new HashMap<>();

    @GetMapping("/{name}/{dataId}")
    public ResponseEntity<byte[]> demo(@PathVariable String name, @PathVariable Integer dataId) throws IOException {
        if (!bitFiles.containsKey(name))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        byte[] bitFile = bitFiles.get(name);
        
        int start = dataId * BITFILE_CHUNK_SIZE;
        int end = dataId * BITFILE_CHUNK_SIZE + BITFILE_CHUNK_SIZE;

        if (dataId * BITFILE_CHUNK_SIZE > bitFile.length)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (end > bitFile.length)
            end = bitFile.length;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("bitFile.bit").build().toString());
        return ResponseEntity.ok().headers(httpHeaders).body(Arrays.copyOfRange(bitFile, start, end));
    }
}
