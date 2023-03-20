package org.ude.es.comm;

public class Status {

    private String status = "";

    private void addToStatus(String keyWord, String value) {
        status += keyWord + ":" + value + ";";
    }

    public Status(String id) {
        addToStatus("ID", id);
    }

    public Status Type(String type) {
        addToStatus("TYPE", type);
        return this;
    }

    public Status Measurement(String measurement) {
        addToStatus("MEASUREMENT", measurement);
        return this;
    }

    public Status State(String state) {
        addToStatus("STATE", state);
        return this;
    }

    public String create() {
        return status;
    }
}
