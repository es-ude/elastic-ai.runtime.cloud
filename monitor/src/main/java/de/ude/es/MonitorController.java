package de.ude.es;

import de.ude.es.Clients.ClientList;
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
            ClientList clientList =
                MonitoringServiceApplication.getClientList();
            model.addAttribute("clients", clientList);
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
            MonitoringServiceApplication.getClientList()
                .changeClientName(ID, name);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("Name set.");
    }
}
