package org.ude.es.communicationEndpoints.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

        String fileName =
            PATH + "/" + new Timestamp(System.currentTimeMillis());
        try {
            FileWriter csvHeader = new FileWriter(fileName, false);
            csvHeader.append("timestamp");
            csvHeader.append(",");
            csvHeader.append("g-value");
            csvHeader.append("\n");
            csvHeader.flush();
            csvHeader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dataRequester.setDataReceiveFunction(data -> {
            try {
                FileWriter csvWriter = new FileWriter(fileName, true);
                csvWriter.append(
                    new Timestamp(System.currentTimeMillis()).toString()
                );
                csvWriter.append(",");
                csvWriter.append(data);
                csvWriter.append("\n");
                csvWriter.flush();
                csvWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        dataRequester.startRequestingData();
    }
}
