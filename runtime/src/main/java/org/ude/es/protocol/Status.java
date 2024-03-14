package org.ude.es.protocol;

public class Status {

    public enum State {

        ONLINE("ONLINE"),
        OFFLINE("OFFLINE");

        final String state;

        State(String state) {
            this.state = state;
        }
    }

    private String ID = null;
    private String TYPE = null;
    private String STATE = null;
    private String DATA = null;
    private String FPGA = null;
    private String VERSION = null;

    public Status(){

    };

    public Status(Status status) {
        this.ID = status.ID;
        this.TYPE = status.TYPE;
        this.STATE = status.STATE;
        this.DATA = status.DATA;
        this.FPGA = status.FPGA;
        this.VERSION = status.VERSION;
    }

    public Status ID(String id) {
        ID = id;
        return this;
    }

    public Status TYPE(String type) {
        TYPE = type;
        return this;
    }

    public Status STATE(State state) {
        STATE = state.toString();
        return this;
    }

    public Status ADD_DATA(String data) {
        if (DATA == null) {
            DATA = data;
        } else {
            DATA += "," + DATA;
        }
        return this;
    }

    public Status SET_DATA(String data) {
        DATA = data;
        return this;
    }

    public Status FPGA(String fpga) {
        FPGA = fpga;
        return this;
    }

    public Status VERSION(String version) {
        VERSION = version;
        return this;
    }

    public Status copy() {
        return new Status(this);
    }

    public String get() {
        if (ID == null) {
            ID = "NULL";
            System.out.println("WARNING: NO ID SET IN STATUS!!!");
        }
        if (TYPE == null) {
            TYPE = "NULL";
            System.out.println("WARNING: NO TYPE SET IN STATUS!!!");
        }
        if (STATE == null) {
            STATE = "NULL";
            System.out.println("WARNING: NO STATE SET IN STATUS!!!");
        }
        String status = String.format("ID:%s;TYPE:%s;STATE:%s;", ID, TYPE, STATE);
        if (DATA != null)
            status += String.format("DATA:%s;", DATA);
        if (FPGA != null)
            status += String.format("FPGA:%s;", FPGA);
        if (VERSION != null)
            status += String.format("DATA:%s;", VERSION);
        return status;
    }
}
