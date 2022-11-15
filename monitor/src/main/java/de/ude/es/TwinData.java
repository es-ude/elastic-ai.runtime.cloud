package de.ude.es;

public class TwinData {

    private String name;
    private final String ID;
    private boolean active;

    public TwinData(String name, String ID) {
        this.ID = fixId(ID);
        this.name = name;
        this.active = true;
    }

    private String fixId(String id) {
        id = id.strip();
        if (id.endsWith("/")) {
            return id.substring(0, id.length() - 1);
        }
        if (id.startsWith("/")) {
            return id.substring(1);
        }
        return id.strip();
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
