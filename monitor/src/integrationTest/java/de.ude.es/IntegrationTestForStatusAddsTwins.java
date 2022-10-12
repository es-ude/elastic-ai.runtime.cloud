package de.ude.es;


import de.ude.es.comm.HivemqBroker;
import de.ude.es.exampleTwins.TwinWithStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
public class IntegrationTestForStatusAddsTwins {
    private static final String            DOMAIN         = "eip://uni-due.de/es";
    private static final String            IP             = "localhost";
    private static       int               PORT;
    private static final int               THREAD_TIMEOUT = 500;
    private              HivemqBroker      broker;
    private              TwinList          twinList;
    private              TwinStatusMonitor statusMonitor;

    @Container public static GenericContainer brokerCont = new GenericContainer(
            DockerImageName.parse( "eclipse-mosquitto:1.6.14" ) ).withExposedPorts(
            1883 );

    @BeforeAll
    static void setUp () {
        PORT = brokerCont.getFirstMappedPort( );
    }

    @BeforeEach
    void initTest () {
        // instantiate broker
        broker = new HivemqBroker( DOMAIN, IP, PORT );

        //instantiate TwinStatusMonitor
        twinList = new TwinList( );
        statusMonitor = new TwinStatusMonitor( twinList );
        statusMonitor.bind( broker );
    }

    @AfterEach
    void cleanAfterTest () {
        broker.closeConnection( );
    }

    @Test
    void SameIdIsNoDuplicate () throws InterruptedException {
        // create first twin
        createTwinWithStatus( "testTwin0" );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );
        // create second twin with same ID
        createTwinWithStatus( "testTwin0" );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );

        // check conditions
        assertEquals( 1, twinList.getTwins( ).size( ) );
        assertEquals( new TwinData( "Twin 0", "/testTwin0" ).toString( ),
                      twinList.getTwins( ).get( 0 ).toString( ) );
    }

    @Test
    void TwinWhoSendOnlineStatusGetsAdded () throws InterruptedException {
        //check list is empty
        assertEquals( 0, twinList.getTwins( ).size( ) );

        // add first twin
        createTwinWithStatus( "testTwin0" );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );
        // check conditions
        assertEquals( 1, twinList.getTwins( ).size( ) );
        assertEquals( new TwinData( "Twin 0", "/testTwin0" ).toString( ),
                      twinList.getTwins( ).get( 0 ).toString( ) );

        // add second twin
        createTwinWithStatus( "testTwin1" );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );
        // check conditions
        assertEquals( 2, twinList.getTwins( ).size( ) );
        assertEquals( new TwinData( "Twin 1", "/testTwin1" ).toString( ),
                      twinList.getTwins( ).get( 1 ).toString( ) );
    }

    @Test
    void twinGetsKicked () throws InterruptedException {
        // add test twin
        var twin = createTwinWithStatus( "testTwin0" );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );
        // check conditions
        assertEquals( 1, twinList.getActiveTwins( ).size( ) );
        assertEquals( new TwinData( "Twin 0", "/testTwin0" ).toString( ),
                      twinList.getTwins( ).get( 0 ).toString( ) );

        // deactivate twin
        twin.sendOfflinePosing( );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );
        // check conditions
        assertEquals( 0, twinList.getActiveTwins( ).size( ) );
        assertEquals( new TwinData( "Twin 0", "/testTwin0", false ).toString( ),
                      twinList.getTwins( ).get( 0 ).toString( ) );
    }

    @Test
    void twinGetsReactivated () throws InterruptedException {
        // create first twin
        var twin = createTwinWithStatus( "testTwin0" );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );
        // check conditions
        assertEquals( 1, twinList.getActiveTwins( ).size( ) );
        assertEquals( new TwinData( "Twin 0", "/testTwin0" ).toString( ),
                      twinList.getTwins( ).get( 0 ).toString( ) );

        // deactivate twin
        twin.sendOfflinePosing( );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );
        // check conditions
        assertEquals( 0, twinList.getActiveTwins( ).size( ) );
        assertEquals( new TwinData( "Twin 0", "/testTwin0" ).toString( ),
                      twinList.getTwins( ).get( 0 ).toString( ) );

        // reactivate twin
        twin.sendOnlinePosting( );
        // sleep to allow mqtt broker to handle new Twin
        Thread.sleep( THREAD_TIMEOUT );
        // check conditions
        assertEquals( 1, twinList.getActiveTwins( ).size( ) );
        assertEquals( new TwinData( "Twin 0", "/testTwin0" ).toString( ),
                      twinList.getTwins( ).get( 0 ).toString( ) );
    }

    private TwinWithStatus createTwinWithStatus ( String identifier ) {
        var sink = new TwinWithStatus( identifier );
        sink.bind( broker );
        sink.sendOnlinePosting( );
        return sink;
    }

}
