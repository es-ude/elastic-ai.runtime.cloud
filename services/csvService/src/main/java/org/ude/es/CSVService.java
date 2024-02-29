package org.ude.es;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.requests.DataRequester;

public class CSVService extends LocalCommunicationEndpoint {

    private static String CAMERA_IP = null;
    private static Integer CAMERA_PORT = null;
    RemoteCommunicationEndpoint enV5;

    private final String PATH = "SensorValues";

    public static void main(String[] args) throws InterruptedException {
        try {
            Namespace arguments = parseArguments(args);
            CAMERA_IP = arguments.getString("camera_address");
            System.out.println(CAMERA_IP);
            CAMERA_PORT = Integer.parseInt(arguments.getString("camera_port"));
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }
        startCommunicationEndpoint(new CSVService(), args);
    }

    public CSVService() {
        super("CSVService");
        enV5 = new RemoteCommunicationEndpoint("enV5");
    }

    private void savePicture(String filePath) {
        for (int i = 0; i < 10; i++) {
            try (
                InputStream ignored = new URL(
                    "http://" + CAMERA_IP + ":" + CAMERA_PORT + "/jpeg"
                ).openStream()
            ) {
                Thread.sleep(10);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try (
            InputStream in = new URL(
                "http://" + CAMERA_IP + ":" + CAMERA_PORT + "/jpeg"
            ).openStream()
        ) {
            Files.copy(in, Paths.get(filePath + "/image.jpg"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("NO PICTURE TAKEN!!!");
        }
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(brokerStub);

        File sensorValueDir = new File(PATH);
        sensorValueDir.mkdir();

        DataRequester dataRequester = new DataRequester(
                enV5,
            "g-value",
            getDomainAndIdentifier()
        );

        dataRequester.setDataReceiveFunction(data -> {
            String fileName =
                PATH + "/" + new Timestamp(System.currentTimeMillis());
            try {
                System.out.println("Saving throw to: " + fileName);

                File csvDir = new File(fileName);
                csvDir.mkdir();

                savePicture(fileName);

                FileWriter csvWriter = new FileWriter(
                    fileName + "/measurement.csv",
                    false
                );

                csvWriter.append("x");
                csvWriter.append(",");
                csvWriter.append("y");
                csvWriter.append(",");
                csvWriter.append("z");
                csvWriter.append("\n");

                csvWriter.append(data.replace(";", "\n"));
                csvWriter.flush();
                csvWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        dataRequester.startRequestingData();
    }

    private static Namespace parseArguments(String[] args)
        throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor(
            "elastic-ai.runtime.CSVService"
        )
            .build()
            .defaultHelp(true)
            .description("Start a csv service for the elastic-ai.runtime");
        ArgumentGroup cameraSpecification = parser.addArgumentGroup(
            "Camera Specification"
        );
        cameraSpecification
            .addArgument("--camera-address")
            .help("Camera Address")
            .setDefault("localhost");
        cameraSpecification
            .addArgument("--camera-port")
            .help("Camera Port")
            .setDefault(8081);

        return parser.parseKnownArgs(args, null);
    }
}
