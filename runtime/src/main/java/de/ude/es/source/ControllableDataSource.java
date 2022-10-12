package de.ude.es.source;


import de.ude.es.comm.Posting;
import de.ude.es.comm.Subscriber;
import de.ude.es.twin.JavaTwin;
import de.ude.es.twin.TwinStub;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple template class for data sources. Can be used by twins that measure
 * some data to make it available to clients. This class handles listening for
 * START and STOP messages and will subscribe for the heartbeats of all its
 * clients to detect if none of them is available anymore and stop sending
 * updates in that case.
 *
 * @param <T> the type of the measured data
 */
public class ControllableDataSource<T> {

    protected final List <Client> clients = new ArrayList <>( );
    protected       JavaTwin      javaTwin;
    protected final String        dataId;

    protected class Client implements Subscriber {

        private final String   heartbeatSource;
        private       boolean  isActive = true;
        private final TwinStub twinStub;

        public Client ( String heartbeatSource ) {
            this.heartbeatSource = heartbeatSource;
            twinStub = new TwinStub( heartbeatSource );
            twinStub.bind( javaTwin.getEndpoint( ) );
            twinStub.subscribeForStatus( this );
        }

        @Override
        public void deliver ( Posting posting ) {
            //System.out.println( "Message: " + posting.data( ) );
            this.isActive = posting.data( ).endsWith( "1" );
        }

        public void stopAndRemoveAndUnsubscribe () {
            if( isActive ) {
                deactivate( );
            }
        }

        private void deactivate () {
            isActive = false;
            clients.remove( this );
            twinStub.unsubscribeFromStatus( this );
        }

        public boolean hasIdentifier ( String identifier ) {
            return heartbeatSource.equals( identifier );
        }
    }

    public ControllableDataSource ( String dataId ) {
        this.dataId = dataId;
    }

    public void bind ( JavaTwin javaTwin ) {
        this.javaTwin = javaTwin;
        javaTwin.subscribeForDataStartRequest( dataId, this::handleNewClient );
        javaTwin.subscribeForDataStopRequest( dataId,
                                              this::handleLeavingClient );
    }

    private void handleNewClient ( Posting posting ) {
        var client = new Client( posting.data( ) );
        clients.add( client );
    }

    private void handleLeavingClient ( Posting posting ) {
        String leavingClientID = posting.data( );
        for( Client client : clients ) {
            if( client.hasIdentifier( leavingClientID ) ) {
                client.stopAndRemoveAndUnsubscribe( );
                return;
            }
        }
    }

    public synchronized boolean hasClients () {
        return !clients.isEmpty( );
    }

    public void set ( T data ) {
        if( hasClients( ) ) {
            javaTwin.publishData( dataId, "" + data );
        }
    }

}
