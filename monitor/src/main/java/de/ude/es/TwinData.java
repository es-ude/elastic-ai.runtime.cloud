package de.ude.es;

public class TwinData {

    private String name;
    private final String ID;
    private boolean active;

    public TwinData(String name, String ID) {
        this.ID = ID;
        this.name = name;
        this.active = true;
    }

    /* FOR TESTING */
    public TwinData(String name, String ID, boolean active) {
        this.ID = ID;
        this.name = name;
        this.active = active;
    }

    public void setActive() {
        this.active = true;
    }

    public void setNotActive() {
        this.active = false;
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
        return (
            "TwinData{" + "name='" + name + '\'' + ", ID='" + ID + '\'' + '}'
        );
    }
}
