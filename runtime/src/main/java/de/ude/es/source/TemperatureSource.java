package de.ude.es.source;

import de.ude.es.util.Timer;

public class TemperatureSource extends ControllableDataSource<Double> {

    public TemperatureSource(Timer timer) {
        super("temperature", timer);
    }

}
