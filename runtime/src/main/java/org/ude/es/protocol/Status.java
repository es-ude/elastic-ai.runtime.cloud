package org.ude.es.protocol;

public class Status {

    private String status;

    private static String getParameter(String parameterType, String value) {
        return parameterType + ":" + value + ";";
    }

    public enum State {
        ONLINE("ONLINE"),
        OFFLINE("OFFLINE");

        private final String state;

        State(String state) {
            this.state = state;
        }

        public String get() {
            return state;
        }
    }

    public enum Parameter {
        ID("ID"),
        MEASUREMENTS("MEASUREMENTS"),
        STATE("STATE");

        private final String parameter;
        private String value = null;

        Parameter(String parameterType) {
            parameter = parameterType;
        }

        public Parameter value(String value) {
            this.value = value;
            return this;
        }

        public String getKey() {
            return parameter;
        }

        public String get() {
            if (value == null) return parameter;
            return getParameter(parameter, value);
        }
    }

    public Status(Status status) {
        this.status = status.status;
    }

    public Status(String ID) {
        status = Parameter.ID.value(ID).get();
    }

    public Status append(Parameter parameter) {
        status += parameter.get();
        return this;
    }

    public String get() {
        return status;
    }
}
