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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.thiesen.hhpt.shared.model.station.Station;
import org.thiesen.hhpt.shared.model.station.StationType;
import org.thiesen.hhpt.shared.model.tag.StationTypeTagKey;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OSMPullParser {
    
    private final XmlPullParser _xpp;
    private final List<StationListener> _listener = new LinkedList<StationListener>();
    
    public OSMPullParser( final XmlPullParser xpp ) {
        _xpp = xpp;
    }
    
    public void registerStationListener( final StationListener listener ) {
        _listener.add( listener );
    }

    public void processDocument() throws XmlPullParserException, IOException
    {
        int eventType = _xpp.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
                processStartElement(  _xpp);
            }
            eventType = _xpp.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);


        fireFinishedListener();
    }

    private void fireFinishedListener() {
        for ( final StationListener listener : _listener ) {
            listener.onStationCreationFinished();
        }
        
    }

    public void processEndElement (final XmlPullParser xpp)
    {
        final String name = xpp.getName();
        final String uri = xpp.getNamespace();
        if ("".equals (uri))
            System.out.println("End element: " + name);
        else
            System.out.println("End element:   {" + uri + "}" + name);
    }


    public void processStartElement(  final XmlPullParser xpp) throws XmlPullParserException, IOException
    {
        final String tagName = xpp.getName();

        if ( "node".equals( tagName ) ) {
            final String id = xpp.getAttributeValue( null, "id" );
            final String longitude = xpp.getAttributeValue( null, "lon" );
            final String latitude = xpp.getAttributeValue( null, "lat" );
            String name = null;
            StationType type = null;
            String operator = null;
            int eventType = _xpp.next();
            do {
                if ( eventType == XmlPullParser.END_TAG && xpp.getName().equals( "node" ) ) {

                    if ( type != null ) {
                        fireListeners( Station.createStation( id, latitude, longitude,  type, name, operator ) );
                        
                        return;
                    }

                    return;   
                }
                if ( eventType == XmlPullParser.START_TAG ) {
                    final XMLTag t = XMLTag.valueOf( xpp.getAttributeValue( null,  "k" ), xpp.getAttributeValue( null,  "v" ) );

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

                eventType = _xpp.next();


            } while ( eventType != XmlPullParser.END_DOCUMENT );
        }

    }

    private void fireListeners( final Station createStation ) {
       for ( final StationListener listener : _listener ) {
           listener.onNewStationCreated( createStation );
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


}
