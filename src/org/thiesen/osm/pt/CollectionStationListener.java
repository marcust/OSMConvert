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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.thiesen.hhpt.shared.model.station.Station;
import org.thiesen.hhpt.shared.model.station.Stations;

public class CollectionStationListener implements StationListener {
    @SuppressWarnings( "unused" )
    private static final Log LOG = LogFactory.getLog( CollectionStationListener.class );

    private final Stations _stations = new Stations();
    
    public void onNewStationCreated( final Station station ) {
        _stations.add( station );
    }

    public void onStationCreationFinished() {
        // do nothing;
    }
    
    public Stations getStations() {
        return _stations;
    }

}
