/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.representations;

import java.util.Map;


/**
 * A 'representation builder' is able to create a representation of a model that is
 * suitable for rendering.
 */
public interface RepresentationBuilder {


    // TODO: Change return type to 'String'? -- it is 'Object' as currently a 'Map' is supported.
    //       'Map' is supported to facilitate use of GSP's when rendering (i.e., the Grails 'render' method
    //       accepts a Map that identifies the GSP (template). 
    /**
     * Builds and returns a representation for the supplied content. This representation is
     * normally a String, although it may also be a Map (hence the 'Object' return type).
     * @param content a Map holding source content to be used when creating the representation
     * @return Object a 'String' or 'Map' representation of the resource that may be rendered to the client
     */
    public Object buildRepresentation( Map content );
    
}
