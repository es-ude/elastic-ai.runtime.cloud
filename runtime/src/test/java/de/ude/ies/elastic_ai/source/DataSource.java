package de.ude.ies.elastic_ai.source;

import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;

/**
 * A simple template class for data sources. Can be used by
 * twins that measure some data to make it available to
 * clients.
 *
 * @param <T> the type of the measured data
 */
public class DataSource<T> {

    protected LocalCommunicationEndpoint localCommunicationEndpoint;

    protected final String dataId;

    public DataSource(String dataId) {
        this.dataId = dataId;
    }

    public void bind(LocalCommunicationEndpoint localCommunicationEndpoint) {
        this.localCommunicationEndpoint = localCommunicationEndpoint;
    }

    public void set(T data) {
        localCommunicationEndpoint.publishData(dataId, "" + data);
    }
}
