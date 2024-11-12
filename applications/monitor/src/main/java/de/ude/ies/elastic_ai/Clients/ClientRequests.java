package de.ude.ies.elastic_ai.Clients;

import de.ude.ies.elastic_ai.Monitor;
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
    public DataValue requestData(@PathVariable String clientID, @PathVariable String dataId) {
        if (Monitor.getClientList().getClient(clientID) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
        }

        ClientData clientData = Monitor.getClientList().getClient(clientID);

        String latest = clientData.getLastDataValues().get(dataId);

        return new DataValue(clientID, dataId, latest);
    }

    @GetMapping("/{clientID}")
    public String clientLandingPage(Model model, @PathVariable String clientID) {
        ClientData client = Monitor.getClientList().getClient(clientID);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("client", client);
        return "client";
    }

    public record DataValue(String CLIENT_ID, String DATA_ID, String VALUE) {}
}
