package com.vaushell.shaarlijavaapi;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * Shaarli client usage examples
 *
 * @author Fabien Vauchelles (fabien AT vauchelles DOT com)
 */
public class Examples
{
//    private final static String LOGIN = "MY_LOGIN";
//    private final static String PASSWORD = "MY_PASSWORD";
//    private final static String ENDPOINT = "http://fabien.vauchelles.com/~fabien/shaarli";
    private final static String LOGIN = "s";
    private final static String PASSWORD = "s";
    private final static String ENDPOINT = "http://localhost/~torus/s/";

    public static void main( String[] args )
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

    /**
     * Show how create/update/delete works
     *
     * @throws IOException
     */
    private static void crudExample()
            throws IOException
    {
        try( ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create a link
            TreeSet<String> tags = new TreeSet<>();
            tags.add( "java" );
            tags.add( "coding" );

            String id = client.createOrUpdateLink( "http://fabien.vauchelles.com/" ,
                                                   "Blog de Fabien Vauchelles" ,
                                                   "Du coooodde rahhh::!!!!!" ,
                                                   tags ,
                                                   false );
            if ( id == null )
            {
                throw new IOException( "Cannot create link. See error log" );
            }

            // Modify a link
            client.createOrUpdateLink( id ,
                                       "http://fabien.vauchelles.com/" ,
                                       "Blég de Fabien" ,
                                       "Encore du code :)" ,
                                       tags ,
                                       false );

            // Delete a link
            client.delete( id );
        }
    }

    /**
     * Show how we can get all tags
     *
     * @throws IOException
     */
    private static void tagsExample()
            throws IOException
    {
        try( ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Name tags
            TreeSet<String> tags = new TreeSet<>();
            tags.add( "java" );
            tags.add( "coding" );

            // Create 10 links
            for ( int i = 0 ; i < 10 ; ++i )
            {
                client.createOrUpdateLink( "monid" + i ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );
            }

            // Get all tags
            for ( Entry<String , Integer> entryTag : client.getTags().entrySet() )
            {
                System.out.println( "Tag: name=" + entryTag.getKey() + ", count=" + entryTag.getValue() );
            }
        }
    }

    /**
     * Show how we get all links
     *
     * @throws IOException
     */
    private static void searchAllExample()
            throws IOException
    {
        try( ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Name tags
            TreeSet<String> tags = new TreeSet<>();
            tags.add( "java" );
            tags.add( "coding" );

            // Create 10 links
            for ( int i = 0 ; i < 10 ; ++i )
            {
                client.createOrUpdateLink( "monid" + i ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );
            }

            // Iterate all links (without restriction)
            Iterator<ShaarliLink> iterator = client.searchAllIterator();
            while ( iterator.hasNext() )
            {
                ShaarliLink link = iterator.next();

                System.out.println( link );
            }
        }
    }

    /**
     * Show how we get all links on page 1
     *
     * @throws IOException
     */
    private static void searchAllPage1example()
            throws IOException
    {
        try( ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Name tags
            TreeSet<String> tags = new TreeSet<>();
            tags.add( "java" );
            tags.add( "coding" );

            // Create 10 links
            for ( int i = 0 ; i < 10 ; ++i )
            {
                client.createOrUpdateLink( "monid" + i ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );
            }

            // Show only 2 links by page
            client.setLinksByPage( 2 );

            // Iterate all links (without restriction)
            for ( ShaarliLink link : client.searchAll( 1 ) )
            {
                System.out.println( link );
            }
        }
    }

    /**
     * Show how we use a term filter
     *
     * @throws IOException
     */
    private static void searchTermExample()
            throws IOException
    {
        try( ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create 10 links
            for ( int i = 0 ; i < 10 ; ++i )
            {
                TreeSet<String> tags = new TreeSet<>();
                tags.add( "java" + i );
                tags.add( "coding" );

                client.createOrUpdateLink( "monid" + i ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );
            }

            // Iterate all links (with tags filter)
            Iterator<ShaarliLink> iterator = client.searchTermIterator( "Blog" );
            while ( iterator.hasNext() )
            {
                ShaarliLink link = iterator.next();

                System.out.println( link );
            }
        }
    }

    /**
     * Show how we use a term filter and get page 1
     *
     * @throws IOException
     */
    private static void searchTermPage1example()
            throws IOException
    {
        try( ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create 10 links
            for ( int i = 0 ; i < 10 ; ++i )
            {
                TreeSet<String> tags = new TreeSet<>();
                tags.add( "java" + i );
                tags.add( "coding" );

                client.createOrUpdateLink( "monid" + i ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );
            }

            // Show only 2 links by page
            client.setLinksByPage( 2 );

            // Iterate all links (without restriction)
            for ( ShaarliLink link : client.searchTerm( 1 ,
                                                        "Blog" ) )
            {
                System.out.println( link );
            }
        }
    }

    /**
     * Show how we use a tags filter
     *
     * @throws IOException
     */
    private static void searchTagsExample()
            throws IOException
    {
        try( ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create 10 links
            for ( int i = 0 ; i < 10 ; ++i )
            {
                TreeSet<String> tags = new TreeSet<>();
                tags.add( "java" + i );
                tags.add( "coding" );

                client.createOrUpdateLink( "monid" + i ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );
            }

            // Iterate all links (with tags filter)
            Iterator<ShaarliLink> iterator = client.searchTagsIterator( "coding" ,
                                                                        "java2" );
            while ( iterator.hasNext() )
            {
                ShaarliLink link = iterator.next();

                System.out.println( link );
            }
        }
    }

    /**
     * Show how we use a tags filter and get page 1
     *
     * @throws IOException
     */
    private static void searchTagsPage1Example()
            throws IOException
    {
        try( ShaarliClient client = new ShaarliClient( ENDPOINT ) )
        {
            if ( !client.login( LOGIN ,
                                PASSWORD ) )
            {
                throw new IOException( "Login error" );
            }

            // Create 10 links
            for ( int i = 0 ; i < 10 ; ++i )
            {
                TreeSet<String> tags = new TreeSet<>();
                tags.add( "java" + i );
                tags.add( "coding" );

                client.createOrUpdateLink( "monid" + i ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "Du coooodde rahhh::!!!!! #" + i ,
                                           tags ,
                                           false );
            }

            // Show only 2 links by page
            client.setLinksByPage( 2 );

            // Iterate all links (without restriction)
            for ( ShaarliLink link : client.searchTags( 1 ,
                                                        "coding" ,
                                                        "java2" ) )
            {
                System.out.println( link );
            }
        }
    }
}
