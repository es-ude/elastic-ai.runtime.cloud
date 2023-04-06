package de.ude.es;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Objects;

import static de.ude.es.BitFile.BITFILE_CHUNK_SIZE;
import static de.ude.es.MonitoringServiceApplication.uploadBifFile;

@Controller
public class MonitorController {

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    @GetMapping({ "/", "/index", "/index.html" })
    public String index(Model model) {
        try {
            TwinList twinList = MonitoringServiceApplication.getTwinList();
            model.addAttribute("twins", twinList);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return "index";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("twinID") String twinID) throws IOException {
        String fileName = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0];
        BitFile.bitFiles.put(fileName, file.getBytes());

        System.out.println("BitFile uploaded: " + ANSI_GREEN + fileName + ANSI_RESET);

        try {
            uploadBifFile(twinID, file.getBytes().length / BITFILE_CHUNK_SIZE);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok("fileName");
    }

    @PostMapping("/changeName")
    @ResponseBody
    public ResponseEntity<?> handleChangeName(
        @RequestParam("name") String name,
        @RequestParam("ID") String ID
    ) {
        try {
            MonitoringServiceApplication.getTwinList().changeTwinName(ID, name);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("Name set.");
    }
}
