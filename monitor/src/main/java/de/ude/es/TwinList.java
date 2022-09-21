package de.ude.es;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TwinList {

    private List<TwinData> twins = new ArrayList<>();

    private final int kikTime;

    public TwinList(int kikTime) {
        this.kikTime = kikTime;
    }

    public void changeTwinName(String ID, String newName) {
        TwinData twin = getTwin(ID);
        if (twin != null) {
            twin.setName(newName);
        }
    }

    public TwinData getTwin(String ID) {
        for (TwinData tw : twins) {
            if (Objects.equals(tw.getID(), ID)) {
                return tw;
            }
        }
        return null;
    }

    public void addTwin(String ID) {
        if (getTwin(ID) == null) {
            twins.add(
                new TwinData(
                    "Twin " + twins.size(),
                    ID,
                    new MonitorTimer(),
                    kikTime
                )
            );
        } else {
            getTwin(ID).resetKickTimer();
        }
    }

    public List<TwinData> getActiveTwins() {
        List<TwinData> activeTwins = new ArrayList<>();
        for (TwinData twin : twins) {
            if (twin.isActive()) {
                activeTwins.add(twin);
            }
        }
        return activeTwins;
    }

    public List<TwinData> getTwins() {
        return twins;
    }
}
