/*
 * $ Id $
 * (c) Copyright 2009 Marcus Thiesen (marcus@thiesen.org)
 *
 *  This file is part of HHPT.
 *
 *  HHPT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  HHPT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with HHPT.  If not, see <http://www.gnu.org/licenses/>.
 *
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
        for ( @SuppressWarnings("unused") final Station station : reader ) {
            count++;
        }
        
        System.out.println( count + " stations read in " + ( System.currentTimeMillis() - startTime ) + "ms");
        
        
    }
    
}
