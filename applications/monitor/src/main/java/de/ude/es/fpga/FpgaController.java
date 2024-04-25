package de.ude.es.fpga;

import de.ude.es.Clients.ClientData;
import de.ude.es.Monitor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping({ "/client/fpga" })
public class FpgaController {

    @GetMapping("/{name}")
    public String enV5ClientLandingPage(
        Model model,
        @PathVariable String name
    ) {
        ClientData client = Monitor.getClientList()
            .getClient(name);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("client", client);
        return "fpga";
    }
}
