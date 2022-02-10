package de.ude.es.source;

import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Protocol;

/**
 * A simple template class for data sources. Can be used by
 * twins that measure some data to make it available to
 * clients.
 * @param <T> the type of the measured data
 */
public class DataSource<T> {

    protected Protocol protocol;

    protected final String dataId;


    public DataSource(String dataId) {
        this.dataId = dataId;
    }

    public void bind(Protocol protocol) {
        this.protocol = protocol;
    }

    public void bind(CommunicationEndpoint endpoint) {
        bind(new Protocol(endpoint));
    }

    public void set(T data) {
        protocol.publishData(dataId,""+data);
    }

}
