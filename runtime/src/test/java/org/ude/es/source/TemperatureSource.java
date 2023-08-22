package org.ude.es.source;

import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;

public class TemperatureSource extends ControllableDataSource<Double> {

    public TemperatureSource(LocalCommunicationEndpoint twin, String dataId) {
        super(twin, dataId);
    }
}
