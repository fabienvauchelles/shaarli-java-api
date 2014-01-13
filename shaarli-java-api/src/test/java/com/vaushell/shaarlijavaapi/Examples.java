/*
 * Copyright (C) 2013 Fabien Vauchelles (fabien_AT_vauchelles_DOT_com).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3, 29 June 2007, of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.vaushell.shaarlijavaapi;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;
import org.joda.time.DateTime;

/**
 * Shaarli client usage examples.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public final class Examples
{
    // PUBLIC
    /**
     * Main class.
     *
     * @param args Arguments
     * @throws Exception
     */
    public static void main( final String... args )
        throws Exception
    {
        crudExample();

        tagsExample();

        searchAllExample();

        searchAllPage1example();

        searchTermExample();

        searchTermPage1example();

        searchTagsExample();

        searchTagsPage1Example();
    }

    // PRIVATE
//    private static final String LOGIN = "MY_LOGIN";
//    private static final String PASSWORD = "MY_PASSWORD";
//    private static final String ENDPOINT = "http://fabien.vauchelles.com/~fabien/shaarli";
    private static final String LOGIN = "s";
    private static final String PASSWORD = "s";
    private static final String ENDPOINT = "http://localhost/~torus/s";

    private Examples()
    {
        // Nothing
    }

    /**
     * Show how create/update/delete works.
     *
     * @throws IOException
     */
    private static void crudExample()
        throws IOException
    {
        try( final ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create a link
            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "java" );
            tags.add( "coding" );

            final String ID = client.createLink( "http://fabien.vauchelles.com/" ,
                                                 "Blog de Fabien Vauchelles" ,
                                                 "Du coooodde rahhh::!!!!!" ,
                                                 tags ,
                                                 false );
            if ( ID == null )
            {
                throw new IOException( "Cannot create link. See error log" );
            }

            // Modify a link
            client.createOrUpdateLink( ID ,
                                       "http://fabien.vauchelles.com/" ,
                                       "Blog de Fabien Vauchelles" ,
                                       "Du coooodde rahhh::!!!!! mais avec des panzanis" ,
                                       tags ,
                                       false );

            // Delete a link
            client.delete( ID );
        }
    }

    /**
     * Show how we can get all tags.
     *
     * @throws IOException
     */
    private static void tagsExample()
        throws IOException
    {
        try( final ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Name tags
            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "java" );
            tags.add( "coding" );

            // Create 10 links
            DateTime t = new DateTime();
            for ( int i = 0 ; i < 10 ; ++i )
            {
                client.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );

                t = t.plusSeconds( 1 );
            }

            // Get all tags
            for ( final Entry<String , Integer> entryTag : client.getTags().entrySet() )
            {
                System.out.println( "Tag: name=" + entryTag.getKey() + ", count=" + entryTag.getValue() );
            }
        }
    }

    /**
     * Show how we get all links.
     *
     * @throws IOException
     */
    private static void searchAllExample()
        throws IOException
    {
        try( final ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Name tags
            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "java" );
            tags.add( "coding" );

            // Create 10 links
            DateTime t = new DateTime();
            for ( int i = 0 ; i < 10 ; ++i )
            {
                client.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );

                t = t.plusSeconds( 1 );
            }

            // Iterate all links (without restriction)
            final Iterator<ShaarliLink> iterator = client.searchAllIterator();
            while ( iterator.hasNext() )
            {
                final ShaarliLink link = iterator.next();

                System.out.println( link );
            }
        }
    }

    /**
     * Show how we get all links on page 1.
     *
     * @throws IOException
     */
    private static void searchAllPage1example()
        throws IOException
    {
        try( final ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Name tags
            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "java" );
            tags.add( "coding" );

            // Create 10 links
            DateTime t = new DateTime();
            for ( int i = 0 ; i < 10 ; ++i )
            {
                client.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );

                t = t.plusSeconds( 1 );
            }

            // Show only 2 links by page
            client.setLinksByPage( 2 );

            // Iterate all links (without restriction)
            for ( final ShaarliLink link : client.searchAll( 1 ) )
            {
                System.out.println( link );
            }
        }
    }

    /**
     * Show how we use a term filter.
     *
     * @throws IOException
     */
    private static void searchTermExample()
        throws IOException
    {
        try( final ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create 10 links
            DateTime t = new DateTime();
            for ( int i = 0 ; i < 10 ; ++i )
            {
                final TreeSet<String> tags = new TreeSet<>();
                tags.add( "java" + i );
                tags.add( "coding" );

                client.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );

                t = t.plusSeconds( 1 );
            }

            // Iterate all links (with tags filter)
            final Iterator<ShaarliLink> iterator = client.searchTermIterator( "Blog" );
            while ( iterator.hasNext() )
            {
                final ShaarliLink link = iterator.next();

                System.out.println( link );
            }
        }
    }

    /**
     * Show how we use a term filter and get page 1.
     *
     * @throws IOException
     */
    private static void searchTermPage1example()
        throws IOException
    {
        try( final ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create 10 links
            DateTime t = new DateTime();
            for ( int i = 0 ; i < 10 ; ++i )
            {
                final TreeSet<String> tags = new TreeSet<>();
                tags.add( "java" + i );
                tags.add( "coding" );

                client.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );

                t = t.plusSeconds( 1 );
            }

            // Show only 2 links by page
            client.setLinksByPage( 2 );

            // Iterate all links (without restriction)
            for ( final ShaarliLink link : client.searchTerm( 1 ,
                                                              "Blog" ) )
            {
                System.out.println( link );
            }
        }
    }

    /**
     * Show how we use a tags filter.
     *
     * @throws IOException
     */
    private static void searchTagsExample()
        throws IOException
    {
        try( final ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create 10 links
            DateTime t = new DateTime();
            for ( int i = 0 ; i < 10 ; ++i )
            {
                final TreeSet<String> tags = new TreeSet<>();
                tags.add( "java" + i );
                tags.add( "coding" );

                client.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );

                t = t.plusSeconds( 1 );
            }

            // Iterate all links (with tags filter)
            final Iterator<ShaarliLink> iterator = client.searchTagsIterator( "coding" ,
                                                                              "java2" );
            while ( iterator.hasNext() )
            {
                final ShaarliLink link = iterator.next();

                System.out.println( link );
            }
        }
    }

    /**
     * Show how we use a tags filter and get page 1.
     *
     * @throws IOException
     */
    private static void searchTagsPage1Example()
        throws IOException
    {
        try( final ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create 10 links
            DateTime t = new DateTime();
            for ( int i = 0 ; i < 10 ; ++i )
            {
                final TreeSet<String> tags = new TreeSet<>();
                tags.add( "java" + i );
                tags.add( "coding" );

                client.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );

                t = t.plusSeconds( 1 );
            }

            // Show only 2 links by page
            client.setLinksByPage( 2 );

            // Iterate all links (without restriction)
            for ( final ShaarliLink link : client.searchTags( 1 ,
                                                              "coding" ,
                                                              "java2" ) )
            {
                System.out.println( link );
            }
        }
    }
}
