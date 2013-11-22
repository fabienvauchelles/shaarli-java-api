/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaushell.shaarlijavaapi;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Shaarli link
 *
 * @author Fabien Vauchelles (fabien AT vauchelles DOT com)
 */
public class ShaarliLink
{
    // PUBLIC
    /**
     * Construct a link
     *
     * @param ID Link's ID
     * @param permaID ID of permalink
     * @param title Link's title
     * @param description Link's description
     * @param url Link's URL
     * @param restricted Is the link private ?
     */
    public ShaarliLink( String ID ,
                        String permaID ,
                        String title ,
                        String description ,
                        String url ,
                        boolean restricted )
    {
        this.ID = ID;
        this.permaID = permaID;
        this.title = title;
        this.description = description;
        this.url = url;
        this.restricted = restricted;
        this.tags = null;
    }

    /**
     * Construct a link
     */
    public ShaarliLink()
    {
        this( null ,
              null ,
              null ,
              null ,
              null ,
              false );
    }

    /**
     * Get the link's ID
     *
     * @return
     */
    public String getID()
    {
        return ID;
    }

    /**
     * Set the link's ID
     *
     * @param ID
     */
    public void setID( String ID )
    {
        this.ID = ID;
    }

    /**
     * Get the link's permalink ID
     *
     * @return
     */
    public String getPermaID()
    {
        return permaID;
    }

    /**
     * Build the permalink with the endpoint
     *
     * @param endpoint from ShaarliClient.getEndpoint()
     * @return Return an URL of the permalink
     * @see ShaarliClient
     */
    public String getPermaURL( String endpoint )
    {
        return endpoint + "/?" + permaID;
    }

    /**
     * Set the link's permalink ID
     *
     * @param permaID
     */
    public void setPermaID( String permaID )
    {
        this.permaID = permaID;
    }

    /**
     * Get the link title
     *
     * @return
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Set the link title
     *
     * @param title
     */
    public void setTitle( String title )
    {
        this.title = title;
    }

    /**
     * Get the link's description
     *
     * @return
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the link's description
     *
     * @param description
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * Get the link URL
     *
     * @return
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Set the link URL
     *
     * @param url
     */
    public void setUrl( String url )
    {
        this.url = url;
    }

    /**
     * Is the link private (true) or public (false) ?
     *
     * @return
     */
    public boolean isRestricted()
    {
        return restricted;
    }

    /**
     * Set the link restriction
     *
     * @param restricted
     */
    public void setRestricted( boolean restricted )
    {
        this.restricted = restricted;
    }

    /**
     * Get all the link's tags. Lazy initialization.
     *
     * @return
     */
    public Set<String> getTags()
    {
        if ( tags == null )
        {
            tags = new TreeSet<>();
        }

        return tags;
    }

    /**
     * Set all the link's tags
     *
     * @param tags
     */
    public void setTags(
            Set<String> tags )
    {
        this.tags = tags;
    }

    /**
     * Add a tag
     *
     * @param tag
     */
    public void addTag( String tag )
    {
        getTags().add( tag );
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode( this.ID );
        hash = 97 * hash + Objects.hashCode( this.permaID );
        hash = 97 * hash + Objects.hashCode( this.title );
        hash = 97 * hash + Objects.hashCode( this.description );
        hash = 97 * hash + Objects.hashCode( this.url );
        hash = 97 * hash + ( this.restricted ? 1 : 0 );
        hash = 97 * hash + Objects.hashCode( this.tags );
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final ShaarliLink other = (ShaarliLink) obj;
        if ( !Objects.equals( this.ID ,
                              other.ID ) )
        {
            return false;
        }
        if ( !Objects.equals( this.permaID ,
                              other.permaID ) )
        {
            return false;
        }
        if ( !Objects.equals( this.title ,
                              other.title ) )
        {
            return false;
        }
        if ( !Objects.equals( this.description ,
                              other.description ) )
        {
            return false;
        }
        if ( !Objects.equals( this.url ,
                              other.url ) )
        {
            return false;
        }
        if ( this.restricted != other.restricted )
        {
            return false;
        }
        if ( !Objects.equals( this.tags ,
                              other.tags ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Link{" + "ID=" + ID + ", permaID=" + permaID + ", title=" + title + ", description=" + description + ", url=" + url + ", restricted=" + restricted + ", tags=" + tags + '}';
    }
    // PRIVATE
    private String ID;
    private String permaID;
    private String title;
    private String description;
    private String url;
    private boolean restricted;
    private Set<String> tags;
}
