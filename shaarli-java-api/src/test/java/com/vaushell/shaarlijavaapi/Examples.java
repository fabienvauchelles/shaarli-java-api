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
    private final static String LOGIN = "MY_LOGIN";
    private final static String PASSWORD = "MY_PASSWORD";
    private final static String ENDPOINT = "http://fabien.vauchelles.com/~fabien/shaarli";

    public static void main( String[] args )
            throws Exception
    {
        crudExample();

        tagsExample();

        searchAllExample();

        searchTermExample();

        searchTagsExample();
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

            // Modify a link
            client.createOrUpdateLink( id ,
                                       "http://fabien.vauchelles.com/" ,
                                       "Blog de Fabien" ,
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
}
