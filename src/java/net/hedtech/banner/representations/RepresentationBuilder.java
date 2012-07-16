/** *****************************************************************************
 Copyright 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
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
