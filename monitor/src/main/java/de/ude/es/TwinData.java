package de.ude.es;

import java.util.ArrayList;
import java.util.List;

public class TwinData {

    private final String ID;
    private String name;
    private boolean active;
    private final ArrayList<String> sensors;

    public TwinData(String name, String ID) {
        this.ID = ID;
        this.name = name;
        this.active = true;
        this.sensors = new ArrayList<>();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive() {
        this.active = true;
    }

    public void setInactive() {
        this.active = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return ID;
    }

    public List<String> getAvailableSensors() {
        return sensors;
    }

    public String toString() {
        return String.format("TwinData{ name='%s', ID='%s' }", name, ID);
    }
}
