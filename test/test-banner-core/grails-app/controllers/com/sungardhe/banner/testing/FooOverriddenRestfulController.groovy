/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.controllers.RestfulControllerMixin

/**
 * Controller supporting the Foo test model using injected RESTful CRUD methods and 
 * that overrides both request parsing (into the params map) and rendering.  This 
 * controller supports custom XML rendering, that adheres to an XML Schema. 
 * @See com.sungardhe.banner.controllers.RestfulControllerMixin comments for
 * a discussion on overriding rendering and params extraction.
 **/
@Mixin(RestfulControllerMixin)
class FooOverriddenRestfulController {

    // HACK -- This property facilitates identifying this controller as having mixed-in REST actions.  It is needed since
    //         the Mixin annotation is not available at runtime, and we need to discover these controllers during bootstrap
    //         in order to register these REST actions so the URI mapping to them succeeds.
    boolean hasRestMixin = true

    def fooService  // injected by Spring
    
    
    // We'll override the 'domainSimpleName' as we are not following conventions and can thus not determine the 
    // the simple domain name from this Controller's class name. (i.e., we want 'Foo' not 'RooRestful')
    public FooOverriddenRestfulController() {
        // NOTE: domainSimpleName and serviceName properties are provided by the mixin.  Since this controller does not
        // follow normal naming conventions (i.e., there is no model named 'FooRestful'), we need this constructor
        // in order to explicitly set the domainSimpleName.
        domainSimpleName = "Foo"

        // Similarly, we must explicitly set serviceName if it cannot be derived from the domainSimpleName using normal conventions.
        // In this case, since we do follow normal conventions for services (based on the domainSimpleName), we don't need to
        // explicitly set this here.
        // serviceName = "fooService"

        // Lastly, we can (optionally) set our own logger (otherwise, logging will be done using a default 'REST API' logger)
        log = Logger.getLogger( this.class )
    }

// ------------------------------------ Custom Renderer --------------------------------------


    // Developer note:
    // Closures that are returned to perform custom rendering must be applicable to the current request format.
    // The closure returned by this method may be able to support multiple formats or just a single format, but
    // it is the responsiblity of this method to return an applicable renderer for the current request.
    // The returned closures are also responsible for setting the appropriate format on the response. They need not
    // set the HTTP status unless it needs to be overriden.
    // If the getCustomRenderer() returns null, default rendering will be used. This method should return a
    // closure only when custom rendering is needed and supported -- otherwise it should return null.
    // If a controller does not require any custom rendering, this method may be removed completely.
    public Closure getCustomRenderer(String actionName) {
        log.trace "getCustomRenderer() invoked with actionName $actionName and will return null"
        null
    }


    // --------------------------- Special 'params' handling & Rendering ---------------------------
    
    
    // We'll return a closure for any specific content type formats that we explicitly support.
    // If we don't return a closure, or if we don't even expose an 'extractParams' method at all, 
    // the base class will check Spring for a registered handler class, and lastly will fall back to 
    // it's default param extraction. 
    //
    public Closure getParamsExtractor() {
        switch (request.header[ "Content-Type" ]) {
            case "application/vnd.sungardhe.student.v0.01+xml" :
                // we'll only add specific support for a custom MIME type
                // TODO: This method won't have to handle content types that have closures registered 
                //       in spring, as the base class will check Spring directly after calling this method. 
                //       That is, only 'special' handling would have to be implemented here. 
                return handleXmlVer0_01
            default:
                return null // the injected action should use rely on default extraction
        }
    }
    
    
    // We override rendering only for cases where it cannot be performed generically in the base class.
    // This includes, for example, rendering XML that is constrained to a versioned XML Schema. 
    //
//  def renderUpdate() {
//      ... any special 'update' rendering (success case and error cases) would go here...    
//  }
    
                
    // TODO: Attain this 'mapper' from a Spring 'ContentTypeHandlerRegistry' bean, using the Content-Type as the key. Base class
    //       should first check for an 'extractParams' method that may return a closure. If no closure is returned,
    //       the base class should check Spring configuration (so we don't need 'extractParams' method just for these cases.)
    //       If no handler is found in Spring configuration, then the base class will attempt default params extraction. 
    // 
    // Since our XML will adhere to other standards, we cannot simply use the built-in Grails converters. 
    // Note that it is important to check for the existence of attributes and 
    // elements before adding to the map that will be used to create or modify the model.  This will allow us to 
    // accept partial XML fragments containing only the information needed to satisfy the request.  
    // For example, to update the foo description, the XML need only populate the description element. 
    // It is important that we do not add to the properties map any other properties (as doing so would set those  
    // property values to null when creating or updating a model instance.)
    def handleXmlVer0_01 = { xml ->
        def props = [:]
        if (xml.@id?.text())                           props.id                 = xml.@id.toInteger()
        if (xml.@systemRequiredIndicator?.text())      props.systemRequiredIndicator = xml.@systemRequiredIndicator?.text()
        if (xml.@lastModifiedBy?.text())               props.lastModifiedBy     = "${xml.@lastModifiedBy.text()}"
        if (xml.@lastModified?.text())                 props.lastModified       = xml.@lastModified.text()
        if (xml.@dataOrigin?.text())                   props.dataOrigin         = "${xml.@dataOrigin.text()}"
        if (xml.@optimisticLockVersion?.text())        props.version            = xml.@optimisticLockVersion.toInteger()
                                                                                
        if (xml.Code?.text())                          props.code               = "${xml.Code.text()}"
        if (xml.Description?.text())                   props.description        = "${xml.Description.text()}"
                                                       
        if (xml.AddressStreetLine1?.text())            props.addressStreetLine1 = xml.AddressStreetLine1?.text()
        if (xml.AddressStreetLine2?.text())            props.addressStreetLine2 = xml.AddressStreetLine2?.text()
        if (xml.AddressStreetLine3?.text())            props.addressStreetLine3 = xml.AddressStreetLine3?.text()
        if (xml.AddressStreetLine4?.text())            props.addressStreetLine4 = xml.AddressStreetLine4?.text()
        if (xml.HouseNumber?.text())                   props.houseNumber        = xml.HouseNumber?.text()
                                                            
        if (xml.AddressCity?.text())                   props.addressCity        = xml.AddressCity?.text()
        if (xml.AddressState?.text())                  props.addressState       = xml.AddressState?.text()
        if (xml.AddressCountry?.text())                props.addressCountry     = xml.AddressCountry?.text()
        if (xml.AddressZipCode?.text())                props.addressZipCode     = xml.AddressZipCode?.text()
        
        if (xml.VoiceResponseMessageNumber?.text())    props.voiceResponseMessageNumber    = xml.VoiceResponseMessageNumber?.text()
        if (xml.StatisticsCanadianInstitution?.text()) props.statisticsCanadianInstitution = xml.StatisticsCanadianInstitution?.text()
        if (xml.DistrictDivision?.text())              props.districtDivision   = xml.DistrictDivision?.text()
        props
    }
                
        
}
