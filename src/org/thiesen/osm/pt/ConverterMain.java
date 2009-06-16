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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.thiesen.hhpt.shared.model.station.StationType;
import org.thiesen.hhpt.shared.model.station.Stations;
import org.thiesen.hhpt.shared.model.tag.StationTypeTagKey;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConverterMain {

    private final static String OSM_SOURCE = "http://download.geofabrik.de/osm/europe/germany/hamburg.osm.bz2";

    public static void main( final String... args ) throws IOException  {
        final InputStream osmStream = getOSMInputStream(); 


        final Document doc = parseXmlFile( osmStream );

        final Element root = getRoot( doc );

        final Stations stations = new Stations();

        final NodeList list = root.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            final Node node = list.item( i );
            if (node instanceof Element) {
                addStationIfValid( (Element)node, stations );        
            }
        }

        System.out.println("Found " + stations.size() + " stations");


        final FileWriter writer = new FileWriter( "stations.csv" );
        writer.write( stations.asFileString() );
        writer.close();

        System.out.println("DONE");

    }


 
    private static InputStream getOSMInputStream() throws ClientProtocolException, IOException {
        final HttpClient client = new DefaultHttpClient();

        final HttpGet get = new HttpGet( OSM_SOURCE );

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


    private static void addStationIfValid( final Element element, final Stations stations ) {
        if ( "node".equalsIgnoreCase( element.getTagName() ) ) {
            final String id = element.getAttribute( "id" );
            final String longitude = element.getAttribute( "lon" );
            final String latitude = element.getAttribute( "lat" );
            String name = null;
            StationType type = null;
            String operator = null;
            final NodeList childs = element.getChildNodes();
            for (int i=0; i< childs.getLength(); i++) {
                final Node node = childs.item( i );
                if (node instanceof Element) {
                    final Element el = (Element) node;

                    final XMLTag t = XMLTag.valueOf( el.getAttribute( "k" ), el.getAttribute( "v" ) );
                    if ( isOpertor( t ) ) {
                        operator = t.getValue();
                    }
                    if ( isName( t ) ) {
                        name = t.getValue();
                    }
                    if ( isStationType( t ) ) {
                        type = StationType.valueOf( t.getKey(), t.getValue() );
                    }

                }
            }

            if ( type != null ) {
                System.out.println("Added " + type + " with name " + name );
                stations.add( id, latitude, longitude, type, name, operator );
            }


        }
    }


    private static boolean isStationType( final XMLTag t ) {
        return t.keyAnyOf( StationTypeTagKey.toXMLKeys() );

    }

    private static boolean isName( final XMLTag t ) {
        return t.keyIs( "name" );
    }

    private static boolean isOpertor( final XMLTag t ) {
        return t.keyIs( "operator" );
    }

    private static Element getRoot( final Document doc ) {
        // Get the XML root node by examining the children nodes
        final NodeList list = doc.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            final Node node = list.item( i );
            if (node instanceof Element) {
                return (Element) node;
            }
        }

        return null;
    }

    public static Document parseXmlFile(final InputStream input ) {
        try {
            // Create a builder factory
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            // Create the builder and parse the file
            final Document doc = factory.newDocumentBuilder().parse( input );
            return doc;
        } catch (final SAXException e) {
            handleException( e );
        } catch (final ParserConfigurationException e) {
            handleException( e );
        } catch (final IOException e) {
            handleException( e );
        }
        return null;
    }

    private static void handleException( final Exception e ) {
        e.printStackTrace();

    }

    
    

}
