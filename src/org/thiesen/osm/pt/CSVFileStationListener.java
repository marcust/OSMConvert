/*
 * $ Id $
 * (c) Copyright 2009 freiheit.com technologies gmbh
 *
 * This file contains unpublished, proprietary trade secret information of
 * freiheit.com technologies gmbh. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * freiheit.com technologies gmbh.
 *
 * Initial version by Marcus Thiesen (marcus.thiesen@freiheit.com)
 */
package org.thiesen.osm.pt;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.thiesen.hhpt.shared.model.station.Station;

public class CSVFileStationListener implements StationListener {
    @SuppressWarnings( "unused" )
    private static final Log LOG = LogFactory.getLog( CSVFileStationListener.class );
    
    private final FileWriter _writer;
    
    public CSVFileStationListener() {
        try {
            _writer = new FileWriter( "stations.csv" );
        } catch ( final IOException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }
    
    public void onNewStationCreated( final Station station ) {
        final StringBuilder builder = new StringBuilder();
        station.appendLineTo( builder ); 
        try {
            _writer.append( builder.toString() );
        } catch ( final IOException e ) {
            e.printStackTrace();
        }
    }

    public void onStationCreationFinished() {
        try {
            _writer.flush();
            _writer.close();

        } catch ( final IOException e ) {
            e.printStackTrace();
        }
        
    }

}
