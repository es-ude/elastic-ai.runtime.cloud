package de.ude.es.Clients;

import de.ude.es.MonitoringServiceApplication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping({ "/client" })
public class ClientRequests {

    @GetMapping("/{clientID}/{dataId}")
    @ResponseBody
    public DataValue requestData(
        @PathVariable String clientID,
        @PathVariable String dataId
    ) {
        if (
            MonitoringServiceApplication.getClientList().getClient(clientID) ==
            null
        ) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Client not found"
            );
        }

        ClientData clientData = MonitoringServiceApplication.getClientList()
            .getClient(clientID);

        String latest = clientData.getLastDataValues().get(dataId);

        return new DataValue(clientID, dataId, latest);
    }

    @GetMapping("/{clientID}")
    public String clientLandingPage(
        Model model,
        @PathVariable String clientID
    ) {
        ClientData client = MonitoringServiceApplication.getClientList()
            .getClient(clientID);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("client", client);
        return "client";
    }

    public record DataValue(String CLIENT_ID, String DATA_ID, String VALUE) {}
}
