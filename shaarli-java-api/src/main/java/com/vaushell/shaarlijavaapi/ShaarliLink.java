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

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Shaarli link.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class ShaarliLink
    implements Serializable
{
    // PUBLIC
    /**
     * Construct a link.
     *
     * @param ID Link's ID
     * @param permaID ID of permalink
     * @param title Link's title
     * @param description Link's description
     * @param url Link's URL
     * @param restricted Is the link private ?
     */
    public ShaarliLink( final String ID ,
                        final String permaID ,
                        final String title ,
                        final String description ,
                        final String url ,
                        final boolean restricted )
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
     * Construct a link.
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
     * Get the link's ID.
     *
     * @return ID the ID
     */
    public String getID()
    {
        return ID;
    }

    /**
     * Set the link's ID.
     *
     * @param ID the ID
     */
    public void setID( final String ID )
    {
        this.ID = ID;
    }

    /**
     * Get the link's permalink ID.
     *
     * @return the permanent ID
     */
    public String getPermaID()
    {
        return permaID;
    }

    /**
     * Build the permalink with the endpoint.
     *
     * @param endpoint from ShaarliClient.getEndpoint()
     * @return Return an URL of the permalink
     * @see ShaarliClient
     */
    public String getPermaURL( final String endpoint )
    {
        return endpoint + "/?" + permaID;
    }

    /**
     * Set the link's permalink ID.
     *
     * @param permaID the permanent ID
     */
    public void setPermaID( final String permaID )
    {
        this.permaID = permaID;
    }

    /**
     * Get the link title.
     *
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Set the link title.
     *
     * @param title The title
     */
    public void setTitle( final String title )
    {
        this.title = title;
    }

    /**
     * Get the link's description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the link's description.
     *
     * @param description The description
     */
    public void setDescription( final String description )
    {
        this.description = description;
    }

    /**
     * Get the link URL.
     *
     * @return the URL
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Set the link URL.
     *
     * @param url The URL
     */
    public void setUrl( final String url )
    {
        this.url = url;
    }

    /**
     * Is the link private (true) or public (false).
     *
     * @return private (true) or public (false)
     */
    public boolean isRestricted()
    {
        return restricted;
    }

    /**
     * Set the link restriction.
     *
     * @param restricted private (true) or public (false)
     */
    public void setRestricted( final boolean restricted )
    {
        this.restricted = restricted;
    }

    /**
     * Get all the link's tags (lazy initialization).
     *
     * @return the tags
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
     * Set all the link's tags.
     *
     * @param tags The tags
     */
    public void setTags( final Set<String> tags )
    {
        this.tags = tags;
    }

    /**
     * Add a tag.
     *
     * @param tag a tag
     */
    public void addTag( final String tag )
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
    public boolean equals( final Object obj )
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
    private static final long serialVersionUID = 12392964032234123L;
    private String ID;
    private String permaID;
    private String title;
    private String description;
    private String url;
    private boolean restricted;
    private Set<String> tags;
}
