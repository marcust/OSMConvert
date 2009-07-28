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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.boehn.kmlframework.kml.Document;
import org.boehn.kmlframework.kml.Kml;
import org.boehn.kmlframework.kml.KmlException;
import org.boehn.kmlframework.kml.Placemark;
import org.thiesen.hhpt.shared.model.station.Station;
import org.thiesen.hhpt.shared.model.station.Stations;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ConverterMain {

    private final static String OSM_SOURCE_HH = "http://download.geofabrik.de/osm/europe/germany/hamburg.osm.bz2";
    private final static String OSM_SOURCE_ND = "http://download.geofabrik.de/osm/europe/germany/niedersachsen.osm.bz2";
    
    
    public static void main( final String... args ) throws IOException, XmlPullParserException, KmlException, HttpException  {
        final Stations stationsHH = downloadHamburg();

        final Stations stationsNd = downloadNiedersachsen();

        
        final Stations allStations = Stations.union( stationsHH, stationsNd );

        
//      final FileWriter writer = new FileWriter( "stations.csv" );
//        
//        writer.write( allStations.asFileString() );
//        writer.close();

        SolrPoster.sendAll( allStations );
        
        //writeKML( allStations );
        
        System.out.println("DONE");

    }



    private static void writeKML( final Stations stations ) throws KmlException, IOException {

        // We create a new KML Document
        final Kml kml = new Kml();

        // We add a document to the kml
        final Document document = new Document();
        kml.setFeature(document);

        
        for ( final Station s : stations ) {
            final Placemark pms = new Placemark( escape( s.getName() ) );
            pms.setDescription( escape( s.getType() +  " " + s.getOperator().stringValue() ).trim() );
            pms.setLocation( s.getPosition().getLongitude().doubleValue(), s.getPosition().getLatitude().doubleValue() );
            
            document.addFeature( pms );
        }
        


        // We generate the kml file
        kml.createKml("stations.kml");

        
    }



    private static String escape( final String name ) {
        return name.replaceAll( "&", "&amp;" ).replaceAll("<", "&lt;").replaceAll( ">", "&gt;" );
        
    }



    private static Stations downloadHamburg() throws ClientProtocolException, IOException, XmlPullParserException {
        final InputStream osmStream = getOSMInputStream( OSM_SOURCE_HH ); 

        final Stations stations = extractStations( osmStream );

        System.out.println("Found " + stations.size() + " stations in Hamburg");
        return stations;
    }

    private static Stations downloadNiedersachsen() throws ClientProtocolException, IOException, XmlPullParserException {
        final InputStream osmStream = getOSMInputStream( OSM_SOURCE_ND ); 

        final Stations stations = extractStations( osmStream );

        System.out.println("Found " + stations.size() + " stations in Niedersachsen");
        return stations;
    }
    
    


    private static Stations extractStations( final InputStream osmStream ) throws XmlPullParserException, IOException {
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        final XmlPullParser xpp = factory.newPullParser();
        xpp.setInput( osmStream, "utf8" );

        final OSMPullParser opp = new OSMPullParser( xpp );
        
        return  opp.processDocument();
    }


 
    private static InputStream getOSMInputStream( final String source ) throws ClientProtocolException, IOException {
        final HttpClient client = new DefaultHttpClient();

        final HttpGet get = new HttpGet( source );

        final HttpResponse response = client.execute( get );

        final HttpEntity entity = response.getEntity();

        if ( entity != null ) {
            final InputStream content = entity.getContent();

            if ( content != null ) {

                content.read( new byte[2], 0, 2 ); // Skip first two bytes, because they are invalid
                
                final CBZip2InputStream bzipStream = new CBZip2InputStream( new BufferedInputStream( content ) );

                return bzipStream;
            }
        }

        return null;
    }


  
    
    

}
