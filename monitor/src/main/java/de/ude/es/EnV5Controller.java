package de.ude.es;

import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping({ "/sensor" })
public class EnV5Controller {

    private record SensorData(String DEVICE_ID, String DATA_ID, float VALUE) {}

    @GetMapping("/{name}")
    public String enV5LandingPage(Model model, @PathVariable String name) {
        TwinData twin = MonitoringServiceApplication
            .getTwinList()
            .getTwin(name);
        if (twin == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("twin", twin);
        return "env5";
    }

    @GetMapping("/{name}/{dataId}")
    @ResponseBody
    public SensorData requestPowerSensorData(
        @PathVariable String name,
        @PathVariable String dataId
    ) {
        if (MonitoringServiceApplication.getTwinList().getTwin(name) == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Device not found"
            );
        }

        if (name.contains("enV5")) {
            try {
                float latest =
                    MonitoringServiceApplication.getLatestMeasurement(
                        name,
                        dataId
                    );

                return new SensorData(name, dataId, latest);
            } catch (TimeoutException t) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Device not reachable"
                );
            }
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
