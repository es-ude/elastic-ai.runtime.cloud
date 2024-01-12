package org.ude.es;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;

import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.requests.DataRequester;

public class CSVService extends LocalCommunicationEndpoint {

    RemoteCommunicationEndpoint enV5Twin;

    private final String PATH = "SensorValues";

    public static void main(String[] args) throws InterruptedException {
        startCommunicationEndpoint(new CSVService(), args);
    }

    public CSVService() {
        super("CSVService");
        enV5Twin = new RemoteCommunicationEndpoint("enV5Twin");
    }

    private void savePicture(String filePath) {
        try(InputStream in = new URL("http://192.168.203.24:8081/jpeg").openStream()){
            Files.copy(in, Paths.get(filePath + "/image.jpg"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("NO PICTURE TAKEN!!!");
        }
    }

    @Override
    protected void executeOnBind() {
        enV5Twin.bindToCommunicationEndpoint(brokerStub);

        File sensorValueDir = new File(PATH);
        sensorValueDir.mkdir();

        DataRequester dataRequester = new DataRequester(
            enV5Twin,
            "g-value",
            getDomainAndIdentifier()
        );

        dataRequester.setDataReceiveFunction(data -> {
            String fileName =
                PATH + "/" + new Timestamp(System.currentTimeMillis());
            try {
                System.out.println(fileName);
                File csvDir = new File(fileName);
                csvDir.mkdir();

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
}
