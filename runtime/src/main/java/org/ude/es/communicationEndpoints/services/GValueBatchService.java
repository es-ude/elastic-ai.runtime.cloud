package org.ude.es.communicationEndpoints.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.requests.DataRequester;

public class GValueBatchService extends LocalCommunicationEndpoint {

    RemoteCommunicationEndpoint enV5Twin;
    private final String PATH = "SensorValues";

    public static void main(String[] args) throws InterruptedException {
        startCommunicationEndpoint(new GValueBatchService(), args);
    }

    public GValueBatchService() {
        super("GValueBatchService");
        enV5Twin = new RemoteCommunicationEndpoint("enV5Twin");
    }

    @Override
    protected void executeOnBind() {
        enV5Twin.bindToCommunicationEndpoint(brokerStub);

        DataRequester dataRequester = new DataRequester(
            enV5Twin,
            "g-value",
            getDomainAndIdentifier()
        );

        File sensorValueDir = new File(PATH);
        sensorValueDir.mkdir();

        dataRequester.setDataReceiveFunction(data -> {
            String fileName = PATH + "/" + data.substring(0, 3);

            data = data.substring(5);

            try {
                FileWriter csvHeader = new FileWriter(fileName, false);
                csvHeader.append("X-Axis");
                csvHeader.append(",");
                csvHeader.append("Y-Axis");
                csvHeader.append(",");
                csvHeader.append("Z-Axis");
                csvHeader.append("\n");
                csvHeader.flush();
                csvHeader.close();

                FileWriter csvWriter = new FileWriter(fileName, true);
                while (data.length() >= 30) {
                    csvWriter.append(data.substring(0, 30));
                    csvWriter.append("\n");
                    data = data.substring(30);
                }

                csvWriter.flush();
                csvWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        dataRequester.startRequestingData();
    }
}
