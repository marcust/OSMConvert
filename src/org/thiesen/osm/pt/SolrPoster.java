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
import java.io.StringWriter;
import java.util.LinkedList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.thiesen.hhpt.shared.model.station.Station;

public class SolrPoster implements StationListener {

    private final LinkedList<Element> _docs = new LinkedList<Element>();

    private void post( final LinkedList<Element> docs ) throws IOException {
        final Element add = new Element( "add" );
        add.addContent( docs );

        final Format format = Format.getPrettyFormat();
        format.setEncoding( "UTF-8" );
        final XMLOutputter fmt = new XMLOutputter( format );

        final StringWriter writer = new StringWriter();

        fmt.output( add, writer );

        postAndCommit( writer.toString() );

    }

    private void postAndCommit( final String content ) throws IOException {
        postToSolr( content );
        postToSolr( "<commit />" );

    }

    private void postToSolr( final String content ) throws IOException {
        final HttpClient client = new DefaultHttpClient();
        final HttpPost method = new HttpPost("http://hhpt-search.appspot.com/update");

        method.setHeader( "Content-Type", "application/xml; charset=utf8" ); 

        method.setEntity( new StringEntity( "<?xml version=\"1.0\" encoding=\"utf-8\"?>"  + content, "utf8" ) );

        client.execute( method );

        System.out.println("Executed update" );
    }

    private Element makeDoc( final Station station ) {
        System.out.println( station.getName() );

        final Element doc = new Element( "doc" );
        addField( doc, "id", station.getId().stringValue() );
        addField( doc, "lat", station.getPosition().getLatitude() );
        addField( doc, "lng", station.getPosition().getLongitude() );
        addField( doc, "stationType", station.getType().name() );
        addField( doc, "stationName", station.getName() );
        addField( doc, "operator", station.getOperator().stringValue() );
        addField( doc, "type", "PUBLIC_TRANSPORT" );
        return doc;
    }

    private void addField( final Element doc, final String name, final Double value ) {
        addField( doc, name, "" + value );

    }

    private void addField( final Element doc, final String name, final String id ) {
        doc.addContent( new Element( "field" ).setAttribute( "name", name ).setText( id ) );

    }


    public void onNewStationCreated( final Station station ) {
        _docs.add( makeDoc( station ) );

        if ( _docs.size() >= 100 ) {
            postAndClearDocs();
        }

    }

    private void postAndClearDocs() {
        try {
            post( _docs );
        } catch ( final IOException e ) {
            e.printStackTrace();
        } 
        _docs.clear();
    }

    public void onStationCreationFinished() {
        postAndClearDocs();
        _docs.clear();
    }

}
