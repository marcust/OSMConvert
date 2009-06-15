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
import java.io.StringWriter;
import java.util.LinkedList;

import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.thiesen.hhpt.shared.model.station.Station;
import org.thiesen.hhpt.shared.model.station.Stations;

public class SolrPoster {

    public static void sendAll( final Stations stations ) throws FileNotFoundException, IOException, HttpException {
        
        final LinkedList<Element> docs = new LinkedList<Element>();
        for ( final Station station : stations ) {
            
            docs.add( makeDoc( station ) );
            
            if ( docs.size() >= 100 ) {
                post( docs );
                docs.clear();
            }
            
            
        }
    }
    
    
    private static void post( final LinkedList<Element> docs ) throws IOException, HttpException {
        final Element add = new Element( "add" );
        add.addContent( docs );
        
        final Format format = Format.getPrettyFormat();
        format.setEncoding( "UTF-8" );
        final XMLOutputter fmt = new XMLOutputter( format );

        final StringWriter writer = new StringWriter();
        
        fmt.output( add, writer );
        
        postAndCommit( writer.toString() );
        
    }

    private static void postAndCommit( final String content ) throws HttpException, IOException {
        postToSolr( content );
        postToSolr( "<commit />" );
        
    }

    private static void postToSolr( final String content ) throws IOException, HttpException {
        final HttpClient client = new DefaultHttpClient();
        final HttpPost method = new HttpPost("http://androit.dyndns.org:8080/localsolr/update");

        method.setHeader( "Content-Type", "application/xml; charset=utf8" ); 
        
        method.setEntity( new StringEntity( "<?xml version=\"1.0\" encoding=\"utf-8\"?>"  + content ) );
        
        client.execute( method );
    }

    private static Element makeDoc( final Station station ) {
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

    private static void addField( final Element doc, final String name, final Double value ) {
        addField( doc, name, "" + value );
        
    }

    private static void addField( final Element doc, final String name, final String id ) {
        doc.addContent( new Element( "field" ).setAttribute( "name", name ).setText( id ) );
        
    }
}
