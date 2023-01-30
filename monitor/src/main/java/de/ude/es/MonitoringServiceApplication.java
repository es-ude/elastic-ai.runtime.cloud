package de.ude.es;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@SpringBootApplication
@RestController
public class MonitoringServiceApplication {

    String twinTableElement =
        """
                              <tr>
                                  <th>NUMBER</th>
                                  <td>
                                      <div id=NAME_ID>NAME</div>
                                  </td>
                                  <td>TWIN_ID</td>
                                  <td><a class="btn btn-primary" href="/TWIN_ID">PAGE</a></td>
                                  <td><button id="NAME_BUTTON_ID" type="button" class="btn btn-secondary" onclick="changeName(this)">Rename</button></td>
                              </tr>
                              """;

    String sensorValueResponseJSON =
        """
                                     {
                                        "device": "DEVICE_ID",
                                        "sensor": "SENSOR_ID",
                                        "type": "VAL_TYPE",
                                        "value": VALUE
                                     }
                                     """;

    String ledStatusJSON =
        """
                           {
                                "led" : "%s",
                                "status" : "%s"
                           }
                           """;

    public void startServer(String[] args) {
        SpringApplication.run(MonitoringServiceApplication.class, args);
    }

    @GetMapping({ "/", "/index" })
    public String index() {
        try {
            File file = ResourceUtils.getFile(
                "src/main/resources/html/index.html"
            );
            String side = new String(Files.readAllBytes(file.toPath()));

            StringBuilder twinTable = new StringBuilder();
            int i = 0;
            for (TwinData tw : Main.getTwinList().getActiveTwins()) {
                twinTable.append(getTwinTableElement(tw, i));
                i++;
            }
            if (Main.getTwinList().getActiveTwins().size() == 0) {
                String start = Pattern.quote("<table id=\"twinTable\"");
                String end = Pattern.quote("<!--twinTable-->");
                side =
                    side.replaceAll(
                        "(" + start + ")" + "[\\d\\D]*" + "(" + end + ")",
                        "<p id=\"twinTable\" class=\"text-center\">No Twins</p>"
                    );
            } else {
                side = side.replace("TABLE_PLACE_HOLDER", twinTable.toString());
            }

            return side;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //    @PostMapping("/upload")
    //    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("twinURI") String twinURI) {
    //        String fileName = file.getOriginalFilename();
    //
    //        Main.twin.getDeviceListReference().changeLastAction(twinURI, "Send file \"" + fileName + "\"");
    //        Main.twin.getDeviceListReference().changeLastFlashedFile(twinURI, fileName);
    //        try {
    //            Main.twin.sendFileToDevice(file, twinURI);
    //        } catch (Exception e) {
    //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    //        }
    //        return ResponseEntity.ok("File uploaded.");
    //    }

    @PostMapping("/changeName")
    public ResponseEntity<?> handleChangeName(
        @RequestParam("name") String name,
        @RequestParam("ID") String ID
    ) {
        try {
            Main.getTwinList().changeTwinName(ID, name);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
        return ResponseEntity.ok("Name set.");
    }

    @GetMapping("/{name}")
    public String loadPage(@PathVariable String name) {
        try {
            String pageToReturn;
            if (name.contains("env5")) {
                pageToReturn =
                    getEnv5Landingpage(Main.getTwinList().getTwin(name));
            } else {
                File file = ResourceUtils.getFile(
                    "src/main/resources/html/" + name + ".html"
                );
                pageToReturn = new String(Files.readAllBytes(file.toPath()));
            }
            return pageToReturn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "404";
    }

    @GetMapping("/{name}/{sensorId}/{value}")
    public String requestPowerSensorData(
        @PathVariable String name,
        @PathVariable String sensorId,
        @PathVariable String value
    ) {
        String response;

        if (Main.getTwinList().getTwin(name) == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Device not found"
            );
        } else if (name.contains("env5")) {
            float latest = Main.getMeasurement(name, sensorId, value);

            response = sensorValueResponseJSON;
            response = response.replace("DEVICE_ID", name);
            response = response.replace("SENSOR_ID", sensorId);
            response = response.replace("VAL_TYPE", value);
            response = response.replace("VALUE", Float.toString(latest));
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @GetMapping("/{name}/led1")
    public String toggleLED(
        @RequestParam String setTo,
        @PathVariable String name
    ) {
        boolean result = false;
        if (setTo.equals("ON")) {
            result = Main.setLED1(name, true);
        } else if (setTo.equals("OFF")) {
            result = Main.setLED1(name, false);
        }
        return (String.format(ledStatusJSON, "led1", result ? "ON" : "OFF"));
    }

    private String getTwinTableElement(TwinData tw, int number) {
        String name = tw.getName();
        String ID = tw.getId();

        String newTableElement = twinTableElement;

        newTableElement = newTableElement.replace("NUMBER", "" + number);
        newTableElement = newTableElement.replace("NAME_ID", (ID + "-name"));
        newTableElement =
            newTableElement.replace("NAME_BUTTON_ID", (ID + "-name-button"));
        newTableElement = newTableElement.replace("NAME", name);
        newTableElement = newTableElement.replace("TWIN_ID", ID);

        return newTableElement;
    }

    private String getEnv5Landingpage(TwinData twin) {
        try {
            File file = ResourceUtils.getFile(
                "src/main/resources/html/env5.html"
            );
            String side = new String(Files.readAllBytes(file.toPath()));
            side = side.replace("TWIN_NAME", twin.getName());
            side = side.replace("TWIN_ID", twin.getId());
            side =
                side.replace(
                    "TWIN_STATUS",
                    twin.isActive() ? "ONLINE" : "OFFLINE"
                );
            if (Main.isLED1On(twin.getId())) {
                side = side.replace("LED1_STATUS", "checked");
            } else {
                side = side.replace("LED1_STATUS", "");
            }
            return side;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
