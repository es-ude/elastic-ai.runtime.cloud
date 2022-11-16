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
                                  <td><button id="NAME_BUTTON_ID" type="button" class="btn btn-secondary" onclick="changeName(this)">Rename</button></td>
                              </tr>
                              """;

    public void startServer(String[] args) {
        SpringApplication.run(MonitoringServiceApplication.class, args);
    }

    @GetMapping("/")
    public String start() {
        return index();
    }

    @GetMapping({ "/index" })
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
        }
        return "404";
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
            File file = ResourceUtils.getFile(
                "src/main/resources/html/" + name
            );
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "404";
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
}
