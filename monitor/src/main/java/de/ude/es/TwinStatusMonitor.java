package de.ude.es;


import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Posting;
import de.ude.es.comm.Subscriber;
import de.ude.es.twin.TwinStub;


public class TwinStatusMonitor {

    private record StatusSubscriber(TwinList twinList) implements Subscriber {

        @Override
        public void deliver ( Posting posting ) {
            System.out.println( posting.data( ) );

            // retrieve data from status posting
            String twinID = posting.data( )
                                   .substring( 0,
                                               posting.data( ).length( ) - 2 );
            boolean twinActive = posting.data( ).endsWith( "1" );

            // check if twin already exists
            TwinData twin = twinList.getTwin( twinID );

            if( twinActive ) {
                // add new twin or set twin active
                twinList.addTwin( twinID );
            } else {
                if( twin != null ) {
                    // set twin to inactive if existing
                    twin.setNotActive( );
                } else {
                    // add non-existing twin and set to inactive
                    twinList.addTwin( twinID );
                    twinList.getTwin( twinID ).setNotActive( );
                }
            }
        }
    }

    private final StatusSubscriber subscriber;
    private final TwinStub         twin;

    public TwinStatusMonitor ( TwinList twinList ) {
        this.subscriber = new StatusSubscriber( twinList );
        this.twin = new TwinStub( "+" );
    }

    public void bind ( CommunicationEndpoint broker ) {
        this.twin.bind( broker );
        this.twin.subscribeForStatus( subscriber );
    }
}
