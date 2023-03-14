package de.ude.es;

import static com.google.common.primitives.UnsignedInteger.ONE;

import com.google.common.primitives.UnsignedInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TwinList {

    private volatile List<TwinData> twins;
    private UnsignedInteger twinIdCounter = ONE;

    public TwinList() {
        twins = new ArrayList<>();
    }

    public void changeTwinName(String ID, String newName)
        throws NullPointerException {
        TwinData twin = getTwin(ID);
        if (twin != null) {
            twin.setName(newName);
        } else {
            throw new NullPointerException("Twin not found!");
        }
    }

    public TwinData getTwin(String ID) {
        for (TwinData twin : twins) {
            if (Objects.equals(twin.getId(), ID)) {
                return twin;
            }
        }
        return null;
    }

    /**
     * if twin already exists -> sets twin.active=true,
     * else -> adds new twin.
     */
    public void addOrUpdateTwin(String ID, String[] measurements) {
        if (getTwin(ID) == null) {
            twins.add(new TwinData("Twin " + twinIdCounter.intValue(), ID));
            twinIdCounter = twinIdCounter.plus(ONE);
        } else {
            getTwin(ID).setActive();
        }
        getTwin(ID).setAvailableSensors(measurements);
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
