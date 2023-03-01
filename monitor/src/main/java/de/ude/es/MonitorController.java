package de.ude.es;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    //    @PostMapping("/upload")
    //    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("twinURI") String twinURI) {
    //        String fileName = file.getOriginalFilename();
    //
    //        MonitoringServiceApplication.twin.getDeviceListReference().changeLastAction(twinURI, "Send file \"" + fileName + "\"");
    //        MonitoringServiceApplication.twin.getDeviceListReference().changeLastFlashedFile(twinURI, fileName);
    //        try {
    //            MonitoringServiceApplication.twin.sendFileToDevice(file, twinURI);
    //        } catch (Exception e) {
    //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    //        }
    //        return ResponseEntity.ok("File uploaded.");
    //    }

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
