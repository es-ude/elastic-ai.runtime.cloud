package de.ude.es.exampleTwins;


import de.ude.es.twin.JavaTwin;


public class TwinWithStatus extends JavaTwin {

    public TwinWithStatus ( String identifier ) {
        super( identifier );
    }

    public void sendOnlinePosting () {
        this.publishStatus( this.identifier, true );

    }

    public void sendOfflinePosing () {
        this.publishStatus( this.identifier, false );
    }

}
