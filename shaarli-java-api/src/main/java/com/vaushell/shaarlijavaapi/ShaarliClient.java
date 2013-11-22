/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaushell.shaarlijavaapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
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
 * @author Fabien Vauchelles (fabien AT vauchelles DOT com)
 */
public class ShaarliClient
        implements AutoCloseable
{
    // PUBLIC
    /**
     * Construct the DAO with a specific http client
     *
     * @param client Specific HTTP client
     * @param endpoint Shaarli endpoint (like http://fabien.vauchelles.com/~fabien/shaarli)
     */
    public ShaarliClient( HttpClient client ,
                          String endpoint )
    {
        if ( client == null || endpoint == null )
        {
            throw new NullPointerException();
        }

        this.cm = null;
        this.client = client;
        this.endpoint = cleanEnding( endpoint );
    }

    /**
     * Construct the DAO
     *
     * @param endpoint Shaarli endpoint (like http://fabien.vauchelles.com/~fabien/shaarli)
     */
    public ShaarliClient( String endpoint )
    {
        if ( endpoint == null )
        {
            throw new NullPointerException();
        }

        this.endpoint = cleanEnding( endpoint );

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setUserAgent( params ,
                                         "Mozilla/5.0 (Windows NT 5.1; rv:15.0) Gecko/20100101 Firefox/15.0.1" );

        SchemeRegistry sr = new SchemeRegistry();

        sr.register( new Scheme( "http" ,
                                 80 ,
                                 PlainSocketFactory.getSocketFactory() ) );

        this.cm = new PoolingClientConnectionManager( sr );
        this.cm.setMaxTotal( 1000 );

        DefaultHttpClient lClient = new DefaultHttpClient( cm ,
                                                           params );
        lClient.setCookieStore( new BasicCookieStore() );

        this.client = lClient;
    }

    /**
     * Return Shaarli endpoint
     *
     * @return endpoint
     */
    public String getEndpoint()
    {
        return endpoint;
    }

    /**
     * Login
     *
     * @param login Login (must not be empty)
     * @param password Password (must not be empty)
     * @return true if logged or false otherwise
     */
    public boolean login( String login ,
                          String password )
    {
        if ( login == null || password == null )
        {
            throw new NullPointerException();
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "[" + getClass().getSimpleName() + "] login() : login=" + login );
        }

        String token;
        try
        {
            token = getToken( endpoint.toString() + "/?do=login" );
        }
        catch( IOException ex )
        {
            logger.error( "Cannot retrieve login token" ,
                          ex );
            return false;
        }

        try
        {
            loginImpl( login ,
                       password ,
                       token );
        }
        catch( IOException ex )
        {
            logger.error( "Cannot retrieve login token" ,
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
     * @param tagsLinks tags (set, no duplicate please)
     * @param restricted Is the link private ?
     * @return generated id
     */
    public String createOrUpdateLink( String url ,
                                      String title ,
                                      String description ,
                                      Set<String> tags ,
                                      boolean restricted )
    {
        return createOrUpdateLink( null ,
                                   url ,
                                   title ,
                                   description ,
                                   tags ,
                                   restricted );
    }

    /**
     * Create or modify a link. To modify, don't forgot the ID !
     *
     * @param id Link's ID. You can enforce one or let it be null (not the permalink id. Don't be confuse!)
     * @param url Link's URL
     * @param title Link's title
     * @param description Link's description
     * @param tags Links tags (set, no duplicate please)
     * @param restricted Is the link private ?
     * @return id
     */
    public String createOrUpdateLink( String id ,
                                      String url ,
                                      String title ,
                                      String description ,
                                      Set<String> tags ,
                                      boolean restricted )
    {
        if ( url == null || title == null )
        {
            throw new NullPointerException();
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    "[" + getClass().getSimpleName() + "] createOrUpdateLink() : id=" + id + " / url=" + url + " / title=" + title + " / description=" + description + " / restricted=" + restricted );
        }

        String token;
        try
        {
            token = getToken( endpoint.toString() + "/?post" );
        }
        catch( IOException ex )
        {
            logger.error( "Cannot retrieve post token" ,
                          ex );
            return null;
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            HttpPost post = new HttpPost( endpoint + "/?post=" + URLEncoder.encode( url ,
                                                                                    "UTF-8" ) );

            List<BasicNameValuePair> nvps = new ArrayList<>();

            String returnID;
            if ( id != null )
            {
                returnID = id;
            }
            else
            {
                returnID = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date() );

            }
            nvps.add( new BasicNameValuePair( "lf_linkdate" ,
                                              returnID ) );

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

            StringBuilder sbTags = new StringBuilder();
            if ( tags != null )
            {
                for ( String tag : tags )
                {
                    if ( sbTags.length() > 0 )
                    {
                        sbTags.append( " " );
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

            post.setEntity( new UrlEncodedFormEntity( nvps ) );

            HttpResponse response = client.execute( post );
            responseEntity = response.getEntity();

            StatusLine sl = response.getStatusLine();
            if ( sl.getStatusCode() != 302 )
            {
                try( InputStream is = responseEntity.getContent() )
                {
                    throw new IOException( IOUtils.toString( is ) );
                }
            }

            return returnID;
        }
        catch( IOException ex )
        {
            logger.error( "Cannot post" ,
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
                catch( IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    /**
     * Delete a link
     *
     * @param id Link's id (not the permalink id. Don't be confuse!)
     * @return
     */
    public boolean delete( String id )
    {
        if ( id == null )
        {
            throw new NullPointerException();
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    "[" + getClass().getSimpleName() + "] delete() : id=" + id );
        }

        String token;
        try
        {
            token = getToken( endpoint.toString() + "/?post" );
        }
        catch( IOException ex )
        {
            logger.error( "Cannot retrieve post token" ,
                          ex );
            return false;
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            HttpPost post = new HttpPost( endpoint + "/?post" );

            List<BasicNameValuePair> nvps = new ArrayList<>();

            nvps.add( new BasicNameValuePair( "lf_linkdate" ,
                                              id ) );

            nvps.add( new BasicNameValuePair( "delete_link" ,
                                              "" ) );

            nvps.add( new BasicNameValuePair( "token" ,
                                              token ) );

            post.setEntity( new UrlEncodedFormEntity( nvps ) );

            HttpResponse response = client.execute( post );
            responseEntity = response.getEntity();

            StatusLine sl = response.getStatusLine();
            if ( sl.getStatusCode() != 302 )
            {
                try( InputStream is = responseEntity.getContent() )
                {
                    throw new IOException( IOUtils.toString( is ) );
                }
            }

            return true;
        }
        catch( IOException ex )
        {
            logger.error( "Cannot delete" ,
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
                catch( IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    /**
     * Get all used tags
     *
     * @return key/value with tag name and tag count
     */
    public Map<String , Integer> getTags()
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    "[" + getClass().getSimpleName() + "] getTags()" );
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            String execURL = endpoint + "/?do=tagcloud";
            HttpGet get = new HttpGet( execURL );

            HttpResponse response = client.execute( get );
            responseEntity = response.getEntity();

            StatusLine sl = response.getStatusLine();
            if ( sl.getStatusCode() == 200 )
            {
                try( InputStream is = responseEntity.getContent() )
                {
                    Map<String , Integer> tags = new TreeMap<String , Integer>();

                    Document doc = Jsoup.parse( is ,
                                                "utf-8" ,
                                                execURL );

                    Elements elts = doc.select( "#cloudtag *" );
                    Iterator<Element> itElts = elts.iterator();
                    while ( itElts.hasNext() )
                    {
                        int count = Integer.parseInt( itElts.next().text() );
                        String name = itElts.next().text();

                        tags.put( name ,
                                  count );
                    }

                    return tags;
                }
            }
            else
            {
                throw new IOException( sl.getReasonPhrase() );
            }
        }
        catch( IOException ex )
        {
            logger.error( "Cannot retrieve tags" ,
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
                catch( IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    /**
     * Iterator to search all links in shaarli
     *
     * @return
     */
    public Iterator<ShaarliLink> searchAllIterator()
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    "[" + getClass().getSimpleName() + "] searchAllIterator()" );
        }

        return iterator( null );
    }

    /**
     * Iterator to search links, filter by a term
     *
     * @param term Term (must not be null)
     * @return an iterator
     */
    public Iterator<ShaarliLink> searchTermIterator( String term )
    {
        if ( term == null )
        {
            throw new NullPointerException();
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    "[" + getClass().getSimpleName() + "] searchTermIterator() : term=" + term );
        }

        try
        {
            return iterator( "searchterm=" + URLEncoder.encode( term ,
                                                                "UTF-8" ) );
        }
        catch( UnsupportedEncodingException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Iterator to search links, filter by tags
     *
     * @param tags Tags array
     * @return an iterator
     */
    public Iterator<ShaarliLink> searchTagsIterator( String... tags )
    {
        if ( tags == null || tags.length <= 0 )
        {
            throw new NullPointerException();
        }

        StringBuilder sb = new StringBuilder();
        for ( String tag : tags )
        {
            if ( sb.length() > 0 )
            {
                sb.append( " " );
            }

            sb.append( tag );
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    "[" + getClass().getSimpleName() + "] searchTagsIterator() : tags=" + sb.toString() );
        }

        try
        {


            return iterator( "searchterm=" + URLEncoder.encode( sb.toString() ,
                                                                "UTF-8" ) );
        }
        catch( UnsupportedEncodingException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Close the Shaarli connection
     */
    @Override
    public void close()
    {
        if ( cm != null )
        {
            cm.shutdown();
        }
    }
    // PRIVATE
    private final static int MAX_LINKS_BY_PAGE = 100;
    private final static Logger logger = LoggerFactory.getLogger( ShaarliClient.class );
    private HttpClient client;
    private PoolingClientConnectionManager cm;
    private String endpoint;

    private String getToken( String execURL )
            throws IOException
    {
        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            HttpGet get = new HttpGet( execURL );

            HttpResponse response = client.execute( get );
            responseEntity = response.getEntity();

            StatusLine sl = response.getStatusLine();
            if ( sl.getStatusCode() != 200 )
            {
                throw new IOException( sl.getReasonPhrase() );
            }

            try( InputStream is = responseEntity.getContent() )
            {
                Document doc = Jsoup.parse( is ,
                                            "utf-8" ,
                                            execURL );

                Elements elts = doc.select( "input[name=token]" );
                if ( elts == null || elts.size() <= 0 )
                {
                    return null;
                }
                else
                {
                    return elts.get( 0 ).attr( "value" );
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

    private void loginImpl( String login ,
                            String password ,
                            String token )
            throws IOException
    {
        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            HttpPost post = new HttpPost( endpoint.toString() + "/?do=login" );

            List<BasicNameValuePair> nvps = new ArrayList<>();
            nvps.add( new BasicNameValuePair( "login" ,
                                              login ) );
            nvps.add( new BasicNameValuePair( "password" ,
                                              password ) );
            nvps.add( new BasicNameValuePair( "token" ,
                                              token ) );
            nvps.add( new BasicNameValuePair( "returnurl" ,
                                              endpoint ) );
            post.setEntity( new UrlEncodedFormEntity( nvps ) );

            HttpResponse response = client.execute( post );
            responseEntity = response.getEntity();

            StatusLine sl = response.getStatusLine();
            if ( sl.getStatusCode() != 302 )
            {
                try( InputStream is = responseEntity.getContent() )
                {
                    throw new IOException( IOUtils.toString( is ) );
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

    private List<ShaarliLink> parseLinks( String execURL )
    {
        if ( execURL == null )
        {
            throw new NullPointerException();
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    "[" + getClass().getSimpleName() + "] parseLinks() : execURL=" + execURL );
        }

        List<ShaarliLink> links = new ArrayList<>();

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            HttpGet get = new HttpGet( execURL );

            HttpResponse response = client.execute( get );
            responseEntity = response.getEntity();

            StatusLine sl = response.getStatusLine();
            if ( sl.getStatusCode() == 200 )
            {
                try( InputStream is = responseEntity.getContent() )
                {
                    Document doc = Jsoup.parse( is ,
                                                "utf-8" ,
                                                execURL );

                    Elements elts = doc.select( "ul li" );
                    if ( elts != null )
                    {
                        for ( Element elt : elts )
                        {
                            boolean restricted;
                            String cssClass = elt.attr( "class" );
                            if ( cssClass != null && cssClass.equals( "private" ) )
                            {
                                restricted = true;
                            }
                            else
                            {
                                restricted = false;
                            }

                            String ID = elt.select( "input[name=lf_linkdate" ).attr( "value" );
                            String permaID = elt.select( "a[name]" ).attr( "id" );
                            String title = elt.select( "span[class=linktitle]" ).text();
                            String description = elt.select( "div[class=linkdescription]" ).text();
                            String url = elt.select( "span[class=linkurl]" ).text();

                            ShaarliLink link = new ShaarliLink( ID ,
                                                                permaID ,
                                                                title ,
                                                                description ,
                                                                url ,
                                                                restricted );

                            Elements eltsTag = elt.select( "div[class=linktaglist] a" );
                            if ( eltsTag != null )
                            {
                                for ( Element eltTag : eltsTag )
                                {
                                    String tag = eltTag.text();

                                    link.addTag( tag );
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
        catch( IOException ex )
        {
            logger.error( "Cannot links" ,
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
                catch( IOException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    private void setLinksByPage( int count )
    {
        if ( count <= 0 )
        {
            throw new IllegalArgumentException();
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    "[" + getClass().getSimpleName() + "] setLinksByPage() : count=" + count );
        }

        HttpEntity responseEntity = null;
        try
        {
            // Exec request
            String execURL = endpoint + "/?linksperpage=" + count;
            HttpGet get = new HttpGet( execURL );

            HttpResponse response = client.execute( get );
            responseEntity = response.getEntity();

            StatusLine sl = response.getStatusLine();
            if ( sl.getStatusCode() != 200 )
            {
                throw new IOException( sl.getReasonPhrase() );
            }
        }
        catch( IOException ex )
        {
            logger.error( "Cannot set links per page" ,
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
                catch( IOException ex )
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
            private List<ShaarliLink> buffer = new ArrayList<>();
            private int bufferCursor = 0;
            private int page = 1;
            private String lastID = null;

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

                    String execURL = endpoint + "/?page=" + ( page++ );
                    if ( query != null && query.length() > 0 )
                    {
                        execURL += "&" + query;
                    }

                    List<ShaarliLink> links = parseLinks( execURL );
                    if ( links.isEmpty() )
                    {
                        return false;
                    }
                    else
                    {
                        String linksLastID = links.get( links.size() - 1 ).getID();
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
        };
    }

    private static String cleanEnding( String url )
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
}
