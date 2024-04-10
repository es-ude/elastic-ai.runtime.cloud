package de.ude.es.enV5;

import static de.ude.es.MonitoringServiceApplication.monitorCommunicationEndpoint;

import de.ude.es.Clients.ClientData;
import de.ude.es.MonitoringServiceApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;

@Controller
@RequestMapping({ "/client/enV5" })
public class EnV5Controller {

    @GetMapping("/{name}")
    public String enV5ClientLandingPage(
        Model model,
        @PathVariable String name
    ) {
        ClientData client = MonitoringServiceApplication.getClientList()
            .getClient(name);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("client", client);
        return "enV5";
    }

    @PostMapping("/measurement/start/{name}")
    public ResponseEntity<Object> startMeasurement(@PathVariable String name) {
        RemoteCommunicationEndpoint clientStub =
            new RemoteCommunicationEndpoint(name);
        clientStub.bindToCommunicationEndpoint(
            monitorCommunicationEndpoint.getBrokerStub()
        );
        clientStub.publishCommand("MEASUREMENTS", "monitor");

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
