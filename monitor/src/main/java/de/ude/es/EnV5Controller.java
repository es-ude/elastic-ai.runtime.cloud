package de.ude.es;

import static de.ude.es.MonitoringServiceApplication.monitorCommunicationEndpoint;

import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;

@Controller
@RequestMapping({ "/sensor" })
public class EnV5Controller {

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

    @GetMapping("/{name}")
    public String enV5LandingPage(Model model, @PathVariable String name) {
        ClientData client = MonitoringServiceApplication.getClientList()
            .getClient(name);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("communicationEndpoint", client);
        return "env5";
    }

    @GetMapping("/{clientID}/{dataId}")
    @ResponseBody
    public SensorData requestPowerSensorData(
        @PathVariable String clientID,
        @PathVariable String dataId
    ) {
        if (
            MonitoringServiceApplication.getClientList().getClient(clientID) == null
        ) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Client not found"
            );
        }

        if (clientID.contains("enV5")) {
            try {
                ClientData clientData = MonitoringServiceApplication
                    .getClientList()
                    .getClient(clientID);
                
                String latest =
                    MonitoringServiceApplication.getLatestMeasurement(
                        clientData.getDataRequester().get(dataId)
                    );

                return new SensorData(clientID, dataId, latest);
            } catch (TimeoutException t) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Client not reachable"
                );
            }
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private record SensorData(String CLIENT_ID, String DATA_ID, String VALUE) {}
}
