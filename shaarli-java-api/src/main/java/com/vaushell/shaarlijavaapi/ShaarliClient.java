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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shaarli client. Now we have a JAVA access to Sebsauvage Shaarli (see http://sebsauvage.net/wiki/doku.php?id=php:shaarli)
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class ShaarliClient
    implements AutoCloseable
{
    // PUBLIC
    /**
     * Construct the DAO with a specific http client.
     *
     * @param client Specific HTTP client
     * @param endpoint Shaarli endpoint (like http://fabien.vauchelles.com/~fabien/shaarli)
     */
    public ShaarliClient( final CloseableHttpClient client ,
                          final String endpoint )
    {
        if ( client == null || endpoint == null )
        {
            throw new IllegalArgumentException();
        }

        this.endpoint = cleanEnding( endpoint );
        this.df = new SimpleDateFormat( "yyyyMMdd_HHmmss" ,
                                        Locale.ENGLISH );
        this.dfPerma = new SimpleDateFormat( "EEE MMM dd HH:mm:ss yyyy -" ,
                                             Locale.ENGLISH );

        this.client = client;
    }

    /**
     * Construct the DAO.
     *
     * @param endpoint Shaarli endpoint (like http://fabien.vauchelles.com/~fabien/shaarli)
     */
    public ShaarliClient( final String endpoint )
    {
        this( HttpClientBuilder
            .create()
            .setDefaultCookieStore( new BasicCookieStore() )
            .setUserAgent( "Mozilla/5.0 (Windows NT 5.1; rv:15.0) Gecko/20100101 Firefox/15.0.1" )
            .build() ,
              endpoint );
    }

    /**
     * Return Shaarli endpoint.
     *
     * @return endpoint
     */
    public String getEndpoint()
    {
        return endpoint;
    }

    /**
     * Login.
     *
     * @param login Login (must not be empty)
     * @param password Password (must not be empty)
     * @return true if logged or false otherwise
     */
    public boolean login( final String login ,
                          final String password )
    {
        if ( login == null || password == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "[" + getClass().getSimpleName() + "] login() : login=" + login );
        }

        final String token;
        try
        {
            token = getToken( endpoint + "/?do=login" );
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot retrieve login token" ,
                          ex );
            return false;
        }

        try
        {
            loginImpl( login ,
                       password ,
                       token );
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot retrieve login token" ,
                          ex );
            return false;
        }

        setLinksByPage( MAX_LINKS_BY_PAGE );

        return true;
    }

    /**
     * Create a link.
     *
     * @param url Link's URL
     * @param title Link's title
     * @param description Link's description
     * @param tags tags (set, no duplicate please)
     * @param restricted Is the link private ?
     * @return generated id (no way to detect if created or not! Use getLinksCount())
     */
    public String createLink( final String url ,
                              final String title ,
                              final String description ,
                              final Set<String> tags ,
                              final boolean restricted )
    {
        return createOrUpdateLink( generateDateID() ,
                                   url ,
                                   title ,
                                   description ,
                                   tags ,
                                   restricted );
    }

    /**
     * Create or modify a link (to modify, don't forgot the ID !).
     *
     * @param ID Link's ID. You can enforce one or let it be null (not the permalink id. Don't be confuse!)
     * @param url Link's URL
     * @param title Link's title
     * @param description Link's description
     * @param tags Links tags (set, no duplicate please)
     * @param restricted Is the link private ?
     * @return id Link's ID (no way to detect if created or not! Use getLinksCount())
     */
    public String createOrUpdateLink( final Date ID ,
                                      final String url ,
                                      final String title ,
                                      final String description ,
                                      final Set<String> tags ,
                                      final boolean restricted )
    {
        return createOrUpdateLink( convertIDdateToString( ID ) ,
                                   url ,
                                   title ,
                                   description ,
                                   tags ,
                                   restricted );
    }

    /**
     * Create or modify a link (to modify, don't forgot the ID !).
     *
     * @param ID Link's ID. You can enforce one or let it be null (not the permalink id. Don't be confuse!)
     * @param url Link's URL
     * @param title Link's title
     * @param description Link's description
     * @param tags Links tags (set, no duplicate please)
     * @param restricted Is the link private ?
     * @return id Link's ID (no way to detect if created or not! Use getLinksCount())
     */
    public String createOrUpdateLink( final String ID ,
                                      final String url ,
                                      final String title ,
                                      final String description ,
                                      final Set<String> tags ,
                                      final boolean restricted )
    {
        if ( url == null || title == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] createOrUpdateLink() : ID=" + ID + " / url=" + url + " / title=" + title + " / description=" + description + " / restricted=" + restricted );
        }

        final String token;
        try
        {
            token = getToken( endpoint + "/?post" );
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot retrieve post token" ,
                          ex );
            return null;
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            final HttpPost post = new HttpPost( endpoint + "/?post=" + URLEncoder.encode( url ,
                                                                                          "UTF-8" ) );

            final List<NameValuePair> nvps = new ArrayList<>();

            nvps.add( new BasicNameValuePair( "lf_linkdate" ,
                                              ID ) );

            nvps.add( new BasicNameValuePair( "lf_url" ,
                                              url ) );

            nvps.add( new BasicNameValuePair( "lf_title" ,
                                              title ) );

            if ( description != null )
            {
                nvps.add( new BasicNameValuePair( "lf_description" ,
                                                  description ) );
            }

            if ( restricted )
            {
                nvps.add( new BasicNameValuePair( "lf_private" ,
                                                  "true" ) );
            }

            final StringBuilder sbTags = new StringBuilder();
            if ( tags != null )
            {
                for ( final String tag : tags )
                {
                    if ( sbTags.length() > 0 )
                    {
                        sbTags.append( ' ' );
                    }

                    sbTags.append( tag );
                }
            }

            if ( sbTags.length() > 0 )
            {
                nvps.add( new BasicNameValuePair( "lf_tags" ,
                                                  sbTags.toString() ) );
            }

            nvps.add( new BasicNameValuePair( "save_edit" ,
                                              "Save" ) );
            nvps.add( new BasicNameValuePair( "token" ,
                                              token ) );
            nvps.add( new BasicNameValuePair( "returnurl" ,
                                              endpoint ) );

            post.setEntity( new UrlEncodedFormEntity( nvps ,
                                                      "UTF-8" ) );

            try( final CloseableHttpResponse response = client.execute( post ) )
            {
                responseEntity = response.getEntity();

                final StatusLine sl = response.getStatusLine();
                if ( sl.getStatusCode() != 302 )
                {
                    try( final InputStream is = responseEntity.getContent() )
                    {
                        throw new IOException( IOUtils.toString( is ) );
                    }
                }
            }

            return ID;
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot post" ,
                          ex );
            return null;
        }
        finally
        {
            if ( responseEntity != null )
            {
                try
                {
                    EntityUtils.consume( responseEntity );
                }
                catch( final IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    /**
     * Delete a link.
     *
     * @param ID Link's id (not the permalink id. Don't be confuse!)
     * @return deleted or not
     */
    public boolean delete( final Date ID )
    {
        if ( ID == null )
        {
            throw new IllegalArgumentException();
        }

        final String IDstr = convertIDdateToString( ID );
        if ( IDstr == null || IDstr.isEmpty() )
        {
            return false;
        }

        return delete( IDstr );
    }

    /**
     * Delete a link.
     *
     * @param ID Link's id (not the permalink id. Don't be confuse!)
     * @return deleted or not
     */
    public boolean delete( final String ID )
    {
        if ( ID == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] delete() : ID=" + ID );
        }

        final String token;
        try
        {
            token = getToken( endpoint + "/?post" );
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot retrieve post token" ,
                          ex );
            return false;
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            final HttpPost post = new HttpPost( endpoint + "/?post" );

            final List<NameValuePair> nvps = new ArrayList<>();

            nvps.add( new BasicNameValuePair( "lf_linkdate" ,
                                              ID ) );

            nvps.add( new BasicNameValuePair( "delete_link" ,
                                              "" ) );

            nvps.add( new BasicNameValuePair( "token" ,
                                              token ) );

            post.setEntity( new UrlEncodedFormEntity( nvps ,
                                                      "UTF-8" ) );

            try( final CloseableHttpResponse response = client.execute( post ) )
            {
                responseEntity = response.getEntity();

                final StatusLine sl = response.getStatusLine();
                if ( sl.getStatusCode() != 302 )
                {
                    try( final InputStream is = responseEntity.getContent() )
                    {
                        throw new IOException( IOUtils.toString( is ) );
                    }
                }

                return true;
            }
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot delete" ,
                          ex );
            return false;
        }
        finally
        {
            if ( responseEntity != null )
            {
                try
                {
                    EntityUtils.consume( responseEntity );
                }
                catch( final IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    /**
     * Delete all links.
     */
    public void deleteAll()
    {
        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] deleteAll()" );
        }

        // Find all IDs
        final HashSet<String> IDs = new HashSet<>();

        final Iterator<ShaarliLink> it = searchAllIterator();
        while ( it.hasNext() )
        {
            final ShaarliLink link = it.next();
            IDs.add( link.getID() );
        }

        // Delete all
        for ( final String ID : IDs )
        {
            delete( ID );
        }
    }

    /**
     * Get all used tags.
     *
     * @return key/value with tag name and tag count
     */
    public Map<String , Integer> getTags()
    {
        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] getTags()" );
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            final String execURL = endpoint + "/?do=tagcloud";
            final HttpGet get = new HttpGet( execURL );

            try( final CloseableHttpResponse response = client.execute( get ) )
            {
                responseEntity = response.getEntity();

                final StatusLine sl = response.getStatusLine();
                if ( sl.getStatusCode() == 200 )
                {
                    try( final InputStream is = responseEntity.getContent() )
                    {
                        final Map<String , Integer> tags = new TreeMap<>();

                        final Document doc = Jsoup.parse( is ,
                                                          "UTF-8" ,
                                                          execURL );

                        final Elements elts = doc.select( "#cloudtag *" );
                        if ( elts != null )
                        {
                            final Iterator<Element> itElts = elts.iterator();
                            while ( itElts.hasNext() )
                            {
                                final Element elt1 = itElts.next();
                                final String countStr = extract( elt1 ,
                                                                 "" ,
                                                                 "" ,
                                                                 "" );
                                if ( countStr == null )
                                {
                                    throw new IOException( "Error during parsing" );
                                }

                                final Element elt2 = itElts.next();
                                final String name = extract( elt2 ,
                                                             "a" ,
                                                             "" ,
                                                             "" );
                                if ( name == null )
                                {
                                    throw new IOException( "Error during parsing" );
                                }

                                try
                                {
                                    tags.put( name ,
                                              Integer.parseInt( countStr ) );
                                }
                                catch( final NumberFormatException ex )
                                {
                                    throw new IOException( "Error during parsing" ,
                                                           ex );
                                }
                            }
                        }

                        return tags;
                    }
                }
                else
                {
                    throw new IOException( sl.getReasonPhrase() );
                }
            }
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot retrieve tags" ,
                          ex );
            return null;
        }
        finally
        {
            if ( responseEntity != null )
            {
                try
                {
                    EntityUtils.consume( responseEntity );
                }
                catch( final IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    /**
     * Iterator to search all links in shaarli. Warning: ID appears only when logged.
     *
     * @return the iterator
     */
    public Iterator<ShaarliLink> searchAllIterator()
    {
        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] searchAllIterator()" );
        }

        return iterator( null );
    }

    /**
     * Get all page's links. Warning: ID appears only when logged.
     *
     * @param page Page number (>=1)
     * @return List of links
     */
    public List<ShaarliLink> searchAll( final int page )
    {
        if ( page < 1 )
        {
            throw new IllegalArgumentException( "page must be greater or equals to 1" );
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] searchAll() : page=" + page );
        }

        final String execURL = endpoint + "/?page=" + page;

        return parseLinks( execURL );
    }

    /**
     * Iterator to search links, filter by a term. Warning: ID appears only when logged.
     *
     * @param term Term (must not be null)
     * @return an iterator
     */
    public Iterator<ShaarliLink> searchTermIterator( final String term )
    {
        if ( term == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] searchTermIterator() : term=" + term );
        }

        try
        {
            return iterator( "searchterm=" + URLEncoder.encode( term ,
                                                                "UTF-8" ) );
        }
        catch( final UnsupportedEncodingException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Get all page's links, filter by a term. Warning: ID appears only when logged.
     *
     * @param page Page number (>=1)
     * @param term Tags array
     * @return List of links
     */
    public List<ShaarliLink> searchTerm( final int page ,
                                         final String term )
    {
        if ( term == null )
        {
            throw new IllegalArgumentException();
        }

        if ( page < 1 )
        {
            throw new IllegalArgumentException( "page must be greater or equals to 1" );
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] searchTerm() : page=" + page + " / term=" + term );
        }

        try
        {
            final String execURL = endpoint + "/?page=" + page + "&searchterm=" + URLEncoder.encode( term ,
                                                                                                     "UTF-8" );

            return parseLinks( execURL );
        }
        catch( final UnsupportedEncodingException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Iterator to search links, filter by tags. Warning: ID appears only when logged.
     *
     * @param tags Tags array
     * @return an iterator
     */
    public Iterator<ShaarliLink> searchTagsIterator( final String... tags )
    {
        if ( tags == null || tags.length <= 0 )
        {
            throw new IllegalArgumentException();
        }

        final StringBuilder sb = new StringBuilder();
        for ( final String tag : tags )
        {
            if ( sb.length() > 0 )
            {
                sb.append( ' ' );
            }

            sb.append( tag );
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] searchTagsIterator() : tags=" + sb.toString() );
        }

        try
        {
            return iterator( "searchtags=" + URLEncoder.encode( sb.toString() ,
                                                                "UTF-8" ) );
        }
        catch( final UnsupportedEncodingException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Get all page's links, filter by tags. Warning: ID appears only when logged.
     *
     * @param page Page number (>=1)
     * @param tags Tags array
     * @return List of links
     */
    public List<ShaarliLink> searchTags( final int page ,
                                         final String... tags )
    {
        if ( page < 1 )
        {
            throw new IllegalArgumentException( "page must be greater or equals to 1" );
        }

        final StringBuilder sb = new StringBuilder();
        for ( final String tag : tags )
        {
            if ( sb.length() > 0 )
            {
                sb.append( ' ' );
            }

            sb.append( tag );
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] searchTags() : page=" + page + " / tags=" + sb.toString() );
        }

        try
        {
            final String execURL = endpoint + "/?page=" + page + "searchtags=" + URLEncoder.encode( sb.toString() ,
                                                                                                    "UTF-8" );

            return parseLinks( execURL );
        }
        catch( final UnsupportedEncodingException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Set the number of links by page.
     *
     * @param count Number of links
     */
    public void setLinksByPage( final int count )
    {
        if ( count <= 0 )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] setLinksByPage() : count=" + count );
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            final String execURL = endpoint + "/?linksperpage=" + count;
            final HttpGet get = new HttpGet( execURL );

            try( final CloseableHttpResponse response = client.execute( get ) )
            {
                responseEntity = response.getEntity();

                final StatusLine sl = response.getStatusLine();
                if ( sl.getStatusCode() != 200 )
                {
                    throw new IOException( sl.getReasonPhrase() );
                }
            }
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot set links per page" ,
                          ex );
        }
        finally
        {
            if ( responseEntity != null )
            {
                try
                {
                    EntityUtils.consume( responseEntity );
                }
                catch( final IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    /**
     * Return the number of links.
     *
     * @return Links count
     */
    public int getLinksCount()
    {
        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] getLinksCount()" );
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            final HttpGet get = new HttpGet( endpoint );

            try( final CloseableHttpResponse response = client.execute( get ) )
            {
                responseEntity = response.getEntity();

                final StatusLine sl = response.getStatusLine();
                if ( sl.getStatusCode() != 200 )
                {
                    throw new IOException();
                }

                try( final InputStream is = responseEntity.getContent() )
                {
                    final Document doc = Jsoup.parse( is ,
                                                      "UTF-8" ,
                                                      endpoint );

//                    final String countStr = extract( doc ,
//                                                     "#pageheader div.nomobile" ,
//                                                     "" ,
//                                                     "\\d+" );
                    final String countStr = extract( doc ,
                                                     "form[name=searchform] input[class=medium]" ,
                                                     "placeholder" ,
                                                     "\\d+" );
                    if ( countStr == null )
                    {
                        return 0;
                    }
                    else
                    {
                        try
                        {
                            return Integer.parseInt( countStr );
                        }
                        catch( final NumberFormatException ex )
                        {
                            return 0;
                        }
                    }
                }
            }
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot get page count" ,
                          ex );

            return 0;
        }
        finally
        {
            if ( responseEntity != null )
            {
                try
                {
                    EntityUtils.consume( responseEntity );
                }
                catch( final IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    /**
     * Close the Shaarli connection.
     *
     * @throws java.io.IOException
     */
    @Override
    public void close()
        throws IOException
    {
        if ( client != null )
        {
            client.close();
        }
    }

    /**
     * Convert a Date ID to a String ID.
     *
     * @param date the Date ID
     * @return the String ID
     */
    public String convertIDdateToString( final Date date )
    {
        if ( date == null )
        {
            return null;
        }
        else
        {
            return df.format( date );
        }
    }

    /**
     * Convert a String ID to a Date ID.
     *
     * @param ID the String ID
     * @return the Date ID
     */
    public Date convertIDstringToDate( final String ID )
    {
        if ( ID == null )
        {
            return null;
        }
        else
        {
            try
            {
                return df.parse( ID );
            }
            catch( final ParseException ex )
            {
                return null;
            }
        }
    }
    // PRIVATE
    private static final int MAX_LINKS_BY_PAGE = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger( ShaarliClient.class );
    private final CloseableHttpClient client;
    private final String endpoint;
    private SimpleDateFormat df;
    private SimpleDateFormat dfPerma;

    private String getToken( final String execURL )
        throws IOException
    {
        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            final HttpGet get = new HttpGet( execURL );

            try( final CloseableHttpResponse response = client.execute( get ) )
            {
                responseEntity = response.getEntity();

                final StatusLine sl = response.getStatusLine();
                if ( sl.getStatusCode() != 200 )
                {
                    throw new IOException( sl.getReasonPhrase() );
                }

                try( final InputStream is = responseEntity.getContent() )
                {
                    final Document doc = Jsoup.parse( is ,
                                                      "UTF-8" ,
                                                      execURL );

                    return extract( doc ,
                                    "input[name=token]" ,
                                    "value" ,
                                    null );
                }
            }
        }
        finally
        {
            if ( responseEntity != null )
            {
                EntityUtils.consume( responseEntity );
            }
        }
    }

    private void loginImpl( final String login ,
                            final String password ,
                            final String token )
        throws IOException
    {
        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            final HttpPost post = new HttpPost( endpoint + "/?do=login" );

            final List<NameValuePair> nvps = new ArrayList<>();
            nvps.add( new BasicNameValuePair( "login" ,
                                              login ) );
            nvps.add( new BasicNameValuePair( "password" ,
                                              password ) );
            nvps.add( new BasicNameValuePair( "token" ,
                                              token ) );
            nvps.add( new BasicNameValuePair( "returnurl" ,
                                              endpoint ) );
            post.setEntity( new UrlEncodedFormEntity( nvps ,
                                                      "UTF-8" ) );

            try( final CloseableHttpResponse response = client.execute( post ) )
            {
                responseEntity = response.getEntity();

                final StatusLine sl = response.getStatusLine();
                if ( sl.getStatusCode() != 302 )
                {
                    try( final InputStream is = responseEntity.getContent() )
                    {
                        throw new IOException( IOUtils.toString( is ) );
                    }
                }
            }
        }
        finally
        {
            if ( responseEntity != null )
            {
                EntityUtils.consume( responseEntity );
            }
        }
    }

    private List<ShaarliLink> parseLinks( final String execURL )
    {
        if ( execURL == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(
                "[" + getClass().getSimpleName() + "] parseLinks() : execURL=" + execURL );
        }

        final List<ShaarliLink> links = new ArrayList<>();

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            final HttpGet get = new HttpGet( execURL );

            try( final CloseableHttpResponse response = client.execute( get ) )
            {
                responseEntity = response.getEntity();

                final StatusLine sl = response.getStatusLine();
                if ( sl.getStatusCode() == 200 )
                {
                    try( final InputStream is = responseEntity.getContent() )
                    {
                        final Document doc = Jsoup.parse( is ,
                                                          "UTF-8" ,
                                                          execURL );

                        final String linkCSSpath = "ul li";
                        final Elements elts = doc.select( linkCSSpath );
                        if ( elts != null )
                        {
                            for ( final Element elt : elts )
                            {
                                final String restrictedStr = extract( elt ,
                                                                      "" ,
                                                                      "class" ,
                                                                      "" );

                                final boolean restricted = "private".equals( restrictedStr );

                                String ID;
                                final String dateStr = extract( elt ,
                                                                "span.linkdate" ,
                                                                "" ,
                                                                ".* - " );
                                if ( dateStr == null )
                                {
                                    ID = null;
                                }
                                else
                                {
                                    try
                                    {
                                        ID = convertIDdateToString( dfPerma.parse( dateStr ) );
                                    }
                                    catch( final ParseException ex )
                                    {
                                        ID = null;
                                    }
                                }

                                final String permaID = extract( elt ,
                                                                "a[name]" ,
                                                                "id" ,
                                                                "" );

                                final String title = extract( elt ,
                                                              "span[class=linktitle]" ,
                                                              "" ,
                                                              "" );

                                final String description = extract( elt ,
                                                                    "div[class=linkdescription]" ,
                                                                    "" ,
                                                                    "" );

                                final String url = extract( elt ,
                                                            "span[class=linkurl]" ,
                                                            "" ,
                                                            "" );

                                final ShaarliLink link = new ShaarliLink( ID ,
                                                                          permaID ,
                                                                          title ,
                                                                          description ,
                                                                          url ,
                                                                          restricted );

                                final Elements eltsTag = elt.select( "div[class=linktaglist] a" );
                                if ( eltsTag != null )
                                {
                                    for ( final Element eltTag : eltsTag )
                                    {
                                        final String tag = extract( eltTag ,
                                                                    "" ,
                                                                    "" ,
                                                                    "" );
                                        if ( tag != null )
                                        {
                                            link.addTag( tag );
                                        }
                                    }
                                }

                                links.add( link );
                            }
                        }

                        return links;
                    }
                }
                else
                {
                    throw new IOException( sl.getReasonPhrase() );
                }
            }
        }
        catch( final IOException ex )
        {
            LOGGER.error( "Cannot links" ,
                          ex );
            return links;
        }
        finally
        {
            if ( responseEntity != null )
            {
                try
                {
                    EntityUtils.consume( responseEntity );
                }
                catch( final IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    private Iterator<ShaarliLink> iterator( final String query )
    {
        return new Iterator<ShaarliLink>()
        {
            // PUBLIC
            @Override
            public boolean hasNext()
            {
                if ( bufferCursor < buffer.size() )
                {
                    return true;
                }
                else
                {
                    buffer.clear();
                    bufferCursor = 0;

                    final List<ShaarliLink> links;
                    if ( query != null && query.length() > 0 )
                    {
                        links = parseLinks( endpoint + "/?page=" + ( page++ ) + "&" + query );
                    }
                    else
                    {
                        links = parseLinks( endpoint + "/?page=" + ( page++ ) );
                    }

                    if ( links.isEmpty() )
                    {
                        return false;
                    }
                    else
                    {
                        final String linksLastID = links.get( links.size() - 1 ).getID();
                        if ( lastID != null && lastID.equals( linksLastID ) )
                        {
                            return false;
                        }
                        else
                        {
                            lastID = linksLastID;

                            buffer.addAll( links );

                            return true;
                        }
                    }
                }
            }

            @Override
            public ShaarliLink next()
            {
                return buffer.get( bufferCursor++ );
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }

            // PRIVATE
            private final List<ShaarliLink> buffer = new ArrayList<>();
            private int bufferCursor;
            private int page = 1;
            private String lastID;
        };
    }

    private static String cleanEnding( final String url )
    {
        if ( url.endsWith( "/" ) )
        {
            return url.substring( 0 ,
                                  url.length() - 1 );
        }
        else
        {
            return url;
        }
    }

    private Long lastGeneratedDate;

    private Date generateDateID()
    {
        synchronized( this )
        {
            if ( lastGeneratedDate != null )
            {
                long diff = System.currentTimeMillis() - lastGeneratedDate;
                while ( diff < 1000 )
                {
                    try
                    {
                        // It's better than a Thread.sleep in a synchronized block
                        wait( 1000 - diff );
                    }
                    catch( final InterruptedException ex )
                    {
                        // Ignore
                    }

                    diff = System.currentTimeMillis() - lastGeneratedDate;
                }
            }

            final Date t = new Date();
            lastGeneratedDate = t.getTime();

            return t;
        }
    }

    private static String extract( final Element source ,
                                   final String cssPath ,
                                   final String attr ,
                                   final String regexp )
    {
        if ( source == null )
        {
            throw new IllegalArgumentException();
        }

        final Element elt;
        if ( cssPath == null || cssPath.isEmpty() )
        {
            elt = source;
        }
        else
        {
            final Elements elts = source.select( cssPath );
            if ( elts.isEmpty() )
            {
                return null;
            }

            elt = elts.first();
        }

        String content;
        if ( attr == null || attr.isEmpty() )
        {
            content = elt.text();
        }
        else
        {
            content = elt.attr( attr );
        }
        if ( content == null )
        {
            return null;
        }
        content = content.trim();

        if ( regexp != null && !regexp.isEmpty() )
        {
            final Pattern p = Pattern.compile( regexp );
            final Matcher m = p.matcher( content );
            if ( m.find() )
            {
                content = m.group().trim();
            }
        }

        if ( content.isEmpty() )
        {
            return null;
        }

        return content;
    }
}
