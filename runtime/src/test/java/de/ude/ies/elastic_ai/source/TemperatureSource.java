package de.ude.ies.elastic_ai.source;

import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;

public class TemperatureSource extends ControllableDataSource<Double> {

    public TemperatureSource(LocalCommunicationEndpoint twin, String dataId) {
        super(twin, dataId);
    }
}
