package de.ude.es;

import de.ude.es.util.MonitorTimer;

public class TwinData {

    public String name;
    public final String ID;
    private final int kikTime;
    private boolean active;
    private final MonitorTimer monitorTimer;

    public TwinData(String name, String ID, MonitorTimer monitorTimer, int kikTime) {
        this.ID = ID;
        this.name = name;
        this.kikTime = kikTime;
        this.active = true;
        this.monitorTimer = monitorTimer;
        startKickTimer();
    }

    /* FOR TESTING */
    public TwinData(String name, String ID, MonitorTimer monitorTimer, int kikTime, boolean active) {
        this.ID = ID;
        this.name = name;
        this.kikTime = kikTime;
        this.active = active;
        this.monitorTimer = monitorTimer;
        startKickTimer();
    }

    private void startKickTimer() {
        monitorTimer.register(kikTime, this::setNotActive);
    }

    public void setNotActive() {
        active = false;
    }

    public void resetKickTimer() {
        active = true;
        startKickTimer();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public boolean isActive() {
        return active;
    }

    public String toString() {
        return "TwinData{" +
                "name='" + name + '\'' +
                ", ID='" + ID + '\'' +
                '}';
    }

}
