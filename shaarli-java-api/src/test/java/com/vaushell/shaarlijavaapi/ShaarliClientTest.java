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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test.
 *
 * @see ShaarliClient
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class ShaarliClientTest
{
    // PUBLIC
    public ShaarliClientTest()
    {
        // Nothing
    }

    /**
     * Initialize the test.
     *
     * @throws ConfigurationException
     */
    @BeforeClass
    public void setUp()
        throws ConfigurationException
    {
        // My config
        String conf = System.getProperty( "conf" );
        if ( conf == null )
        {
            conf = "conf-local/configuration.xml";
        }

        final XMLConfiguration config = new XMLConfiguration( conf );

        // New format
        final ShaarliTemplates templates = new ShaarliTemplates();
        final List<HierarchicalConfiguration> cTemplates = config.configurationsAt( "shaarli.templates.template" );
        if ( cTemplates != null )
        {
            for ( final HierarchicalConfiguration cTemplate : cTemplates )
            {
                templates.add( cTemplate.getString( "[@key]" ) ,
                               cTemplate.getString( "[@csspath]" ) ,
                               cTemplate.getString( "[@attribut]" ) ,
                               cTemplate.getString( "[@regex]" ) );
            }
        }

        clientUnauth = new ShaarliClient( templates ,
                                          config.getString( "shaarli.endpoint" ) );

        clientAuth = new ShaarliClient( templates ,
                                        config.getString( "shaarli.endpoint" ) );
        clientAuth.login( config.getString( "shaarli.login" ) ,
                          config.getString( "shaarli.password" ) );
    }

    /**
     * Remove all links before each test.
     */
    @BeforeMethod
    public void cleanAndCheck()
    {
        clientAuth.deleteAll();

        assertEquals( "Delete all should remove all links" ,
                      0 ,
                      clientAuth.getLinksCount() );
    }

    /**
     * Test CRUD.
     */
    @Test
    public void testCRUD()
    {
        // Create
        final TreeSet<String> tags = new TreeSet<>();
        tags.add( "java" );
        tags.add( "coding" );

        final String ID = clientAuth.createLink( "http://fabien.vauchelles.com/" ,
                                                 "Blog de Fabien Vauchelles n°" ,
                                                 "Du coooodde rahhh::!!!!! #" ,
                                                 tags ,
                                                 false );

        // Create check
        assertTrue( "ID should be assigned" ,
                    ID != null && !ID.isEmpty() );

        List<ShaarliLink> links = clientUnauth.searchAll( 1 );
        assertEquals( "Only 1 links should be created" ,
                      1 ,
                      links.size() );
        ShaarliLink link = links.get( 0 );

        assertEquals( "IDs must be the same" ,
                      ID ,
                      link.getID() );
        assertEquals( "URLs must be the same" ,
                      "http://fabien.vauchelles.com/" ,
                      link.getUrl() );
        assertEquals( "Titles must be the same" ,
                      "Blog de Fabien Vauchelles n°" ,
                      link.getTitle() );
        assertEquals( "Descriptions must be the same" ,
                      "Du coooodde rahhh::!!!!! #" ,
                      link.getDescription() );
        assertFalse( "Link must not be restricted" ,
                     link.isRestricted() );
        assertArrayEquals( "Tags must be the same" ,
                           tags.toArray() ,
                           link.getTags().toArray() );

        // Modify
        final TreeSet<String> tags2 = new TreeSet<>();
        tags2.add( "codding" );
        tags2.add( "blogging" );
        clientAuth.createOrUpdateLink( ID ,
                                       "http://ma.nouvelle.url.com/" ,
                                       "Nouveau titre" ,
                                       "Nouvelle description" ,
                                       tags2 ,
                                       true );

        // Modify check
        links = clientUnauth.searchAll( 1 );
        assertEquals( "Link must have disappear from public view" ,
                      0 ,
                      links.size() );
        links = clientAuth.searchAll( 1 );
        assertEquals( "A modification shouldn't create another link" ,
                      1 ,
                      links.size() );
        link = links.get( 0 );

        assertEquals( "IDs must be the same" ,
                      ID ,
                      link.getID() );
        assertEquals( "URIs must be the same" ,
                      "http://ma.nouvelle.url.com/" ,
                      link.getUrl() );
        assertEquals( "Titles must be the same" ,
                      "Nouveau titre" ,
                      link.getTitle() );
        assertEquals( "Descriptions must be the same" ,
                      "Nouvelle description" ,
                      link.getDescription() );
        assertTrue( "Link must be private" ,
                    link.isRestricted() );
        assertArrayEquals( "Tags must be the same" ,
                           tags2.toArray() ,
                           link.getTags().toArray() );

        // Delete
        clientAuth.delete( ID );

        assertEquals( "Link shouldn't exist here" ,
                      0 ,
                      clientAuth.getLinksCount() );
    }

    /**
     * Test a link creation for a unlogged user.
     */
    @Test
    public void testCreateUnlogged()
    {
        final TreeSet<String> tags = new TreeSet<>();
        tags.add( "java" );
        tags.add( "coding" );

        clientUnauth.createLink( "http://fabien.vauchelles.com/1" ,
                                 "Blog de Fabien Vauchelles n°1" ,
                                 "Du coooodde rahhh::!!!!! #" ,
                                 tags ,
                                 false );

        assertEquals( "Unlogged user can't create links" ,
                      0 ,
                      clientUnauth.getLinksCount() );
    }

    /**
     * Test if burst mecanism protects from the same ID generation.
     */
    @Test
    public void testBurstMecanism()
    {
        final TreeSet<String> tags = new TreeSet<>();
        tags.add( "java" );
        tags.add( "coding" );

        clientAuth.createLink( "http://fabien.vauchelles.com/1" ,
                               "Blog de Fabien Vauchelles n°1" ,
                               "Du coooodde rahhh::!!!!! #" ,
                               tags ,
                               false );

        final TreeSet<String> tags2 = new TreeSet<>();
        tags2.add( "java" );
        tags2.add( "coding" );

        clientAuth.createLink( "http://fabien.vauchelles.com/2" ,
                               "Blog de Fabien Vauchelles n°2" ,
                               "Du code, du vrai" ,
                               tags2 ,
                               false );

        assertEquals( "2 quick creations can't have the same id (one creation per second)" ,
                      2 ,
                      clientAuth.getLinksCount() );
    }

    /**
     * Test search all.
     */
    @Test
    public void testSearchAll()
    {
        // Create
        Date t = new Date();
        for ( int i = 0 ; i < 10 ; i++ )
        {
            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "tagfix" );
            tags.add( "tag" + i );

            clientAuth.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "du java quoi! #" + i ,
                                           tags ,
                                           false );

            t = new Date( t.getTime() + 1000 );
        }

        // Check
        assertEquals( "10 links should have been created" ,
                      10 ,
                      clientAuth.getLinksCount() );

        clientUnauth.setLinksByPage( 3 );

        int num = 9;
        final Iterator<ShaarliLink> it = clientUnauth.searchAllIterator();
        while ( it.hasNext() )
        {
            final ShaarliLink link = it.next();

            assertEquals( "URLs must be the same" ,
                          "http://fabien.vauchelles.com/" + num ,
                          link.getUrl() );
            assertEquals( "Titles must be the same" ,
                          "Blog de Fabien Vauchelles n°" + num ,
                          link.getTitle() );
            assertEquals( "Descriptions must be the same" ,
                          "du java quoi! #" + num ,
                          link.getDescription() );
            assertFalse( "Link must be public" ,
                         link.isRestricted() );

            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "tagfix" );
            tags.add( "tag" + num );
            assertArrayEquals( "Tags must be the same" ,
                               tags.toArray() ,
                               link.getTags().toArray() );

            --num;
        }
    }

    /**
     * Test search all reverse.
     */
    @Test
    public void testSearchAllReverse()
    {
        // Create
        Date t = new Date();
        for ( int i = 0 ; i < 10 ; i++ )
        {
            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "tagfix" );
            tags.add( "tag" + i );

            clientAuth.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "du java quoi! #" + i ,
                                           tags ,
                                           false );

            t = new Date( t.getTime() + 1000 );
        }

        // Check
        assertEquals( "10 links should have been created" ,
                      10 ,
                      clientAuth.getLinksCount() );

        clientUnauth.setLinksByPage( 3 );

        int num = 0;
        final Iterator<ShaarliLink> it = clientUnauth.searchAllReverseIterator();
        while ( it.hasNext() )
        {
            final ShaarliLink link = it.next();

            assertEquals( "URLs must be the same" ,
                          "http://fabien.vauchelles.com/" + num ,
                          link.getUrl() );
            assertEquals( "Titles must be the same" ,
                          "Blog de Fabien Vauchelles n°" + num ,
                          link.getTitle() );
            assertEquals( "Descriptions must be the same" ,
                          "du java quoi! #" + num ,
                          link.getDescription() );
            assertFalse( "Link must be public" ,
                         link.isRestricted() );

            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "tagfix" );
            tags.add( "tag" + num );
            assertArrayEquals( "Tags must be the same" ,
                               tags.toArray() ,
                               link.getTags().toArray() );

            ++num;
        }
    }

    /**
     * Test search term.
     */
    @Test
    public void testSearchTerm()
    {
        final TreeSet<String> tags = new TreeSet<>();
        tags.add( "java" );
        tags.add( "coding" );

        Date t = new Date();
        clientAuth.createOrUpdateLink( t ,
                                       "http://fabien.vauchelles.com/1" ,
                                       "Blog de Fabien Vauchelles n°1" ,
                                       "Du coooodde rahhh::!!!!! #" ,
                                       tags ,
                                       false );

        t = new Date( t.getTime() + 1000 );
        final TreeSet<String> tags2 = new TreeSet<>();
        tags2.add( "java" );
        tags2.add( "coding" );

        clientAuth.createOrUpdateLink( t ,
                                       "http://fabien.vauchelles.com/2" ,
                                       "Blog de Fabien Vauchelles n°2" ,
                                       "Du code, du vrai" ,
                                       tags2 ,
                                       false );

        assertEquals( "2 links should have been created" ,
                      2 ,
                      clientAuth.getLinksCount() );

        final Iterator<ShaarliLink> it = clientAuth.searchTermIterator( "code" );
        assertTrue( "Search should work" ,
                    it.hasNext() );

        final ShaarliLink link = it.next();
        assertEquals( "Search should find the good result" ,
                      "Blog de Fabien Vauchelles n°2" ,
                      link.getTitle() );
    }

    /**
     * Test search tag.
     */
    @Test
    public void testSearchTag()
    {
        final TreeSet<String> tags = new TreeSet<>();
        tags.add( "java" );
        tags.add( "coding" );

        Date t = new Date();
        clientAuth.createOrUpdateLink( t ,
                                       "http://fabien.vauchelles.com/1" ,
                                       "Blog de Fabien Vauchelles n°1" ,
                                       "Du coooodde rahhh::!!!!! #" ,
                                       tags ,
                                       false );

        t = new Date( t.getTime() + 1000 );
        final TreeSet<String> tags2 = new TreeSet<>();
        tags2.add( "coding" );
        tags2.add( "blogging" );

        clientAuth.createOrUpdateLink( t ,
                                       "http://fabien.vauchelles.com/2" ,
                                       "Blog de Fabien Vauchelles n°2" ,
                                       "Du code, du vrai" ,
                                       tags2 ,
                                       false );

        assertEquals( "2 links should have been created" ,
                      2 ,
                      clientAuth.getLinksCount() );

        final Iterator<ShaarliLink> it = clientAuth.searchTagsIterator( "java" );
        assertTrue( "Search should work" ,
                    it.hasNext() );

        final ShaarliLink link = it.next();
        assertEquals( "Search should find the good result" ,
                      "Blog de Fabien Vauchelles n°1" ,
                      link.getTitle() );
    }

    /**
     * Test tags creation.
     */
    @Test
    public void testTags()
    {
        // Create link 1
        final TreeSet<String> tags = new TreeSet<>();
        tags.add( "java" );
        tags.add( "coding" );

        final String ID = clientAuth.createLink( "http://fabien.vauchelles.com/" ,
                                                 "Blog de Fabien Vauchelles n°" ,
                                                 "Du coooodde rahhh::!!!!! #" ,
                                                 tags ,
                                                 false );

        assertTrue( "ID should be assigned" ,
                    ID != null && !ID.isEmpty() );

        // Create link 2
        final TreeSet<String> tags2 = new TreeSet<>();
        tags2.add( "java" );
        tags2.add( "blogging" );

        final String ID2 = clientAuth.createLink( "http://www.vauchelles.com/" ,
                                                  "Site Vauchelles n°" ,
                                                  "Stout" ,
                                                  tags2 ,
                                                  false );

        assertTrue( "ID should be assigned" ,
                    ID2 != null && !ID2.isEmpty() );

        final Map<String , Integer> atags = clientAuth.getTags();
        assertEquals( "Tags count must be 3" ,
                      3 ,
                      atags.size() );

        assertEquals( "Keyword 'blogging' must exists 1 time" ,
                      1 ,
                      (int) atags.get( "blogging" ) );
        assertEquals( "Keyword 'coding' must exists 1 time" ,
                      1 ,
                      (int) atags.get( "coding" ) );
        assertEquals( "Keyword 'java' must exists 2 times" ,
                      2 ,
                      (int) atags.get( "java" ) );
    }

    /**
     * Test setLinksByPage.
     */
    @Test
    public void testSetLinksByPage()
    {
        // Create
        Date t = new Date();
        for ( int i = 0 ; i < 10 ; i++ )
        {
            final TreeSet<String> tags = new TreeSet<>();
            tags.add( "tagfix" );
            tags.add( "tag" + i );

            clientAuth.createOrUpdateLink( t ,
                                           "http://fabien.vauchelles.com/" + i ,
                                           "Blog de Fabien Vauchelles n°" + i ,
                                           "du java quoi! #" + i ,
                                           tags ,
                                           false );

            t = new Date( t.getTime() + 1000 );
        }

        for ( int i = 1 ; i <= 10 ; i++ )
        {
            clientAuth.setLinksByPage( i );

            final List<ShaarliLink> links = clientAuth.searchAll( 1 );

            assertEquals( "Links set and count must match" ,
                          i ,
                          links.size() );
        }

    }

    // PRIVATE
    private ShaarliClient clientUnauth;
    private ShaarliClient clientAuth;
}
