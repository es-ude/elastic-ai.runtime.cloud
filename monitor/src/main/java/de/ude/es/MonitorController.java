package de.ude.es;

import static de.ude.es.BitFileController.uploadBitFile;

import java.io.IOException;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class MonitorController {

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
