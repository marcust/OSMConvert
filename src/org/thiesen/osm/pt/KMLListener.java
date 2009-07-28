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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.boehn.kmlframework.kml.Document;
import org.boehn.kmlframework.kml.Kml;
import org.boehn.kmlframework.kml.KmlException;
import org.boehn.kmlframework.kml.Placemark;
import org.thiesen.hhpt.shared.model.station.Station;

public class KMLListener implements StationListener {
    @SuppressWarnings( "unused" )
    private static final Log LOG = LogFactory.getLog( KMLListener.class );
    private final Kml _kml;
    private final Document _document;

    public KMLListener() {
        // We create a new KML Document
        _kml = new Kml();

        // We add a document to the kml
        _document = new Document();
        _kml.setFeature( _document );

    }
      

    public void onNewStationCreated( final Station s ) {
        final Placemark pms = new Placemark( escape( s.getName() ) );
        pms.setDescription( escape( s.getType() +  " " + s.getOperator().stringValue() ).trim() );
        pms.setLocation( s.getPosition().getLongitude().doubleValue(), s.getPosition().getLatitude().doubleValue() );

        _document.addFeature( pms );


    }

    public void onStationCreationFinished() {
        try {
            _kml.createKml("stations.kml");
        } catch ( final KmlException e ) {
            e.printStackTrace();
        } catch ( final IOException e ) {
            e.printStackTrace();
        }
    }

    private static final String escape( final String name ) {
        return name.replaceAll( "&", "&amp;" ).replaceAll("<", "&lt;").replaceAll( ">", "&gt;" );
    }

}
