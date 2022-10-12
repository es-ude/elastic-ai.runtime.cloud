package de.ude.es;


import de.ude.es.comm.HivemqBroker;
import de.ude.es.twin.JavaTwin;


public class Main {

    public static        TwinList twinList;
    private static final String   DOMAIN = "eip://uni-due.de/es";

    public static void main ( String[] args ) {
        twinList = new TwinList( );
        HivemqBroker broker = new HivemqBroker( DOMAIN );

        var sink = new JavaTwin( "monitor" );
        sink.bind( broker );

        TwinStatusMonitor twinStatusMonitor = new TwinStatusMonitor( twinList );
        twinStatusMonitor.bind( broker );

        MonitoringServiceApplication serviceApplication
                = new MonitoringServiceApplication( );
        serviceApplication.startServer( args );
    }

}
