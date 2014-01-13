/*
 * Copyright (C) 2014 Fabien Vauchelles (fabien_AT_vauchelles_DOT_com).
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

import java.util.HashMap;

/**
 * How to find usefull information in a Shaarli template. See source to get the keys.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class ShaarliTemplates
{
    // PUBLIC
    /**
     * Constructor. Initialize defaults keys.
     */
    public ShaarliTemplates()
    {
        this.templates = new HashMap<>();

        // Date format for ID
        add( "id-dateformat" ,
             "yyyyMMdd_HHmmss" ,
             "" ,
             "" );

        // Date format show in permalink
        add( "permalink-dateformat" ,
             "EEE MMM dd HH:mm:ss yyyy -" ,
             "" ,
             "" );

        // Where the list of tags starts, in tag cloud
        add( "cloudtag" ,
             "#cloudtag *" ,
             "" ,
             "" );

        // Inside the tag cloud list, where we could find the name
        add( "cloudtag-name" ,
             "" ,
             "" ,
             "" );

        // Inside the tag cloud list, where we could find the tag count
        add( "cloudtag-count" ,
             "" ,
             "" ,
             "\\d+" );

        // Where we find the global links count
        add( "links-count" ,
             "#pageheader div.nomobile" ,
             "" ,
             "\\d+" );

        // Where we find a token
        add( "token" ,
             "input[name=token]" ,
             "value" ,
             "" );

        // Where the list of links starts, in a listing
        add( "links" ,
             "ul li" ,
             "" ,
             "" );

        // Inside the links list, where we could find if the link is private
        add( "links-private" ,
             "li[class=private]" ,
             "class" ,
             "" );

        // Inside the links list, where we could find the link's date id
        add( "links-id" ,
             "span.linkdate" ,
             "" ,
             ".* - " );

        // Inside the links list, where we could find the link's permalink
        add( "links-permalink-id" ,
             "a[name]" ,
             "id" ,
             "" );

        // Inside the links list, where we could find the link's title
        add( "links-title" ,
             "span[class=linktitle]" ,
             "" ,
             "" );

        // Inside the links list, where we could find the link's description
        add( "links-description" ,
             "div[class=linkdescription]" ,
             "" ,
             "" );

        // Inside the links list, where we could find the link's url
        add( "links-url" ,
             "span[class=linkurl]" ,
             "" ,
             "" );

        // Inside the links list, where the list of tags starts
        add( "tags" ,
             "div[class=linktaglist] a" ,
             "" ,
             "" );

        // Inside the tags list, where we could find the tag count, and name (count first happens, and name second happens)
        add( "tags-tag" ,
             "" ,
             "" ,
             "" );

        // Get page max number
        add( "page-max" ,
             "#paging_current" ,
             "" ,
             "(\\d+)$" );
    }

    /**
     * Add a template.
     *
     * @param key Template's key
     * @param cssPath Template's CSS path (how to find the information). Could be empty (not null)
     * @param attribut Tag attribute (if empty, not null, it uses the text. Otherwise, it uses the attribut content.
     * @param regex Regex to parse the match (could be empty, not null)
     */
    public void add( final String key ,
                     final String cssPath ,
                     final String attribut ,
                     final String regex )
    {
        if ( key == null || cssPath == null || attribut == null || regex == null )
        {
            throw new IllegalArgumentException();
        }

        templates.put( key ,
                       new Template( cssPath ,
                                     attribut ,
                                     regex ) );
    }

    /**
     * Get a template.
     *
     * @param key Template's key.
     * @return the Template
     */
    public Template get( final String key )
    {
        if ( key == null )
        {
            throw new IllegalArgumentException();
        }

        return templates.get( key );
    }

    // DEFAULT
    /**
     * A template.
     */
    public static final class Template
    {
        // DEFAULT
        final String cssPath;
        final String attribut;
        final String regex;

        Template( final String cssPath ,
                  final String attribut ,
                  final String regex )
        {
            this.cssPath = cssPath;
            this.attribut = attribut;
            this.regex = regex;
        }
    }

    // PRIVATE
    private final HashMap<String , Template> templates;
}
