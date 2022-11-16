package de.ude.es;

public class TwinData {

    private final String ID;
    private String name;
    private boolean active;

    public TwinData(String name, String ID) {
        this.ID = ID;
        this.name = name;
        this.active = true;
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

    public String toString() {
        return String.format("TwinData{ name='%s', ID='%s' }", name, ID);
    }
}
