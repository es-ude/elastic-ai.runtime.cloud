package org.ude.es.source;

import org.ude.es.twinBase.JavaTwin;

public class TemperatureSource extends ControllableDataSource<Double> {

    public TemperatureSource(JavaTwin twin, String dataId) {
        super(twin, dataId);
    }
}
