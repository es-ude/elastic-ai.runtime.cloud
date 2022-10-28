package org.ude.es.source;

import org.ude.es.twinBase.JavaTwin;

/**
 * A simple template class for data sources. Can be used by
 * twins that measure some data to make it available to
 * clients.
 *
 * @param <T> the type of the measured data
 */
public class DataSource<T> {

    protected JavaTwin javaTwin;

    protected final String dataId;

    public DataSource(String dataId) {
        this.dataId = dataId;
    }

    public void bind(JavaTwin javaTwin) {
        this.javaTwin = javaTwin;
    }

    public void set(T data) {
        javaTwin.publishData(dataId, "" + data);
    }
}
