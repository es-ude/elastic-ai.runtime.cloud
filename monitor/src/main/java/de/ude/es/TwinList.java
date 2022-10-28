package de.ude.es;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TwinList {

    private List<TwinData> twins;

    public TwinList() {
        twins = new ArrayList<>();
    }

    public void changeTwinName(String ID, String newName) {
        TwinData twin = getTwin(ID);
        if (twin != null) {
            twin.setName(newName);
        }
    }

    public TwinData getTwin(String ID) {
        for (TwinData twin : twins) {
            if (Objects.equals(twin.getID(), ID)) {
                return twin;
            }
        }
        return null;
    }

    /**
     * if twin already exists -> sets twin.active=true,
     * else -> adds new twin.
     */
    public void addTwin(String ID) {
        if (getTwin(ID) == null) {
            twins.add(new TwinData("Twin " + twins.size(), ID));
        } else {
            getTwin(ID).setActive();
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
