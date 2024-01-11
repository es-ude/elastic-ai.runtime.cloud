package org.ude.es;

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

        dataRequester.setDataReceiveFunction(data -> {
            String fileName =
                    PATH + "/" + new Timestamp(System.currentTimeMillis());
            try {
                System.out.println(fileName);
                File csvDir = new File(fileName);
                csvDir.mkdir();

                FileWriter csvWriter = new FileWriter(fileName + "/measurement.csv", false);
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
