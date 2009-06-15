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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.thiesen.hhpt.shared.io.StationReader;
import org.thiesen.hhpt.shared.model.station.Station;


public class ReaderMain {

    public static void main( final String... args ) throws FileNotFoundException, IOException {
        
        
        final StationReader reader = new StationReader("stations.csv");
        
        long count = 0;
        final long startTime = System.currentTimeMillis();
        for ( final Station station : reader ) {
            count++;
        }
        
        System.out.println( count + " stations read in " + ( System.currentTimeMillis() - startTime ) + "ms");
        
        
    }
    
}
