package de.ude.es;

public class TwinData {

    public String name;
    public final String ID;

    public TwinData(String name, String ID) {
        this.ID = ID;
        this.name = name;
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

    public String toString() {
        return "TwinData{" +
                "name='" + name + '\'' +
                ", ID='" + ID + '\'' +
                '}';
    }

}
