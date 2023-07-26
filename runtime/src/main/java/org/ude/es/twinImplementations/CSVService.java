package org.ude.es.twinImplementations;

import static org.ude.es.twinBase.Executable.startTwin;

import java.io.File;
import java.sql.Timestamp;
import org.ude.es.protocol.DataRequester;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

import java.io.FileWriter;
import java.io.IOException;

public class CSVService extends JavaTwin {

    TwinStub enV5Twin;

    private final String PATH = "SensorValues";

    public static void main(String[] args) throws InterruptedException {
        startTwin(new CSVService(), args);
    }

    public CSVService() {
        super("CSVService");
        enV5Twin = new TwinStub("enV5Twin");
    }

    @Override
    protected void executeOnBind() {
        enV5Twin.bindToCommunicationEndpoint(endpoint);

        DataRequester dataRequester = new DataRequester(enV5Twin, "g-value", getDomainAndIdentifier());

        File sensorValueDir = new File(PATH);
        sensorValueDir.mkdir();

        String fileName = PATH + "/" + new Timestamp(System.currentTimeMillis());
        try {
            FileWriter csvHeader = new FileWriter(fileName , false);
            csvHeader.append("timestamp");
            csvHeader.append(",");
            csvHeader.append("g-value");
            csvHeader.append("\n");
            csvHeader.flush();
            csvHeader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dataRequester.addWhenNewDataReceived(data -> {
            try {
                FileWriter csvWriter = new FileWriter(fileName, true);
                csvWriter.append(new Timestamp(System.currentTimeMillis()).toString());
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
