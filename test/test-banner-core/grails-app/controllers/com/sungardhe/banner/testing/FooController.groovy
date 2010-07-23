/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import org.apache.log4j.Logger
import grails.util.GrailsNameUtils

/**
 * Controller supporting the 'Foo' model that relies on mixed-in RESTful CRUD methods.
 * Note that the actions used to expose a RESTful interface are provided by the RestfulControllerMixin class,
 * which is mixed in at runtime during bootstrap (see BannerCoreGrailsPlugin.groovy).
 **/
class FooController  {

    // The mixInRestActions lists actions that will be mixed into this class and that will be registered with the corresponding Grails 'artefact'.
    // Note that all REST actions implemented by RestfulControllerMixin will be mixed-in, only those specified here will
    // be registered with the artefact (so that URL mapping will be allowed only to these actions).
    static List mixInRestActions = [ 'show', 'list', 'create', 'update', 'destroy' ]

    def fooService  // injected by Spring

    def invokedRenderCallbacks = [] // this is used for testing the framework -- it is NOT something controllers would normally have


    public FooRestfulController() {
        // Note: domainSimpleName and serviceName properties are provided by the mixin.  Since this controller follows
        //       normal naming conventions, we don't need to set the domain simple name here.
        // domainSimpleName = "Foo"

        // Note: Similarly, we must explicitly set serviceName if it cannot be derived from the domainSimpleName using normal conventions.
        //       In this case, since we do follow normal conventions for services (based on the domainSimpleName), we don't need to
        //       explicitly set this here.
        // serviceName = "fooService"

        // Lastly, we can (optionally) set our own logger (otherwise, logging will be done using a default 'REST API' logger)
        log = Logger.getLogger( this.class )
    }


    // ---------------------------- User Interface Actions (non-RESTful) ----------------------------------
    // The following actions are here solely to illustrate that additional actions can be implemented here.
    // In general, our controllers will support only RESTful clients for integration purposes, and will
    // not have non-RESTful actions like 'index' or 'view'.  It IS possible to invoke the mixed-in actions
    // non-restfully if necessary (i.e., by using a URL that specifies the action name).


    // in case someone uses a URI explicitly indicating 'index' or our URI mapping includes a non-RESTful mapping to 'index'
    def index = {
        redirect( action: "view", params: params )
    }


    // Render main User Interface page -- note that ALL other actions are provided by the base class. :-)
    def view = {
        render "If I had a UI, I'd render it now!"
        // Render the main ZUL page supporting this model.  All subseqent requests from this UI will be
        // handled by the corresponding 'Composer'.  This is the +only+ action supporting the ZK based user interface.
        // The other actions in this controller support RESTful clients.
    }


    // ------------------------------------ Custom Renderer --------------------------------------

    // Developer note:
    // Closures that are returned to perform custom rendering must be applicable to the current request format.
    // The closure returned by this method may be able to support multiple formats or just a single format, but
    // it is the responsibility of this method to return an applicable renderer for the current request.
    // The returned closures are also responsible for setting the appropriate format on the response. They need not
    // set the HTTP status unless it needs to be overridden.
    //
    // If the getCustomRenderer() returns null, default rendering will be used. This method should return a
    // closure only when custom rendering is needed and supported -- otherwise it should return null.
    // If a controller does not require any custom rendering, this method may be removed completely.
    public Closure getCustomRenderer( String actionName ) {
        log.trace "getCustomRenderer() invoked with actionName $actionName, and the request format is ${request.getHeader( 'Content-Type' )}"
        invokedRenderCallbacks << actionName // not normally present, this is used solely for testing the framework
                
        // We'll only provide custom rendering for the application/vnd.sungardhe.student.v0.01+xml MIME type
        if ("application/vnd.sungardhe.student.v0.01+xml" != request.getHeader( 'Content-Type' )) {
            return null
        }

        response.setHeader( "Content-Type", "application/vnd.sungardhe.student.v0.01+xml" )

        // refBase is used for the 'Ref' element, which provides a link within the rendered XML back to the RESTful endpoint
        def refBase = "${request.scheme}://${request.serverName}:${request.serverPort}/${grailsApplication.metadata.'app.name'}/${GrailsNameUtils.getPropertyNameRepresentation( domainSimpleName )}"

        switch (actionName) {
            // TODO: Use a StreamingMarkupBuilder to build up the response, versus using GSP templates
            case 'list'   : return { renderDataMap -> render( template:"list.v1.0.xml",
                                                              model: [ fooList: renderDataMap.data, totalCount: renderDataMap.totalCount, refBase: refBase ] ) }
            case 'show'   : return { renderDataMap -> render( template:"single.v1.0.xml",
                                                              model: [ foo: renderDataMap.data, refBase: refBase ] ) }
            case 'create' : return { renderDataMap -> render( template:"single.v1.0.xml",
                                                              model: [ foo: renderDataMap.data, refBase: refBase ] ) }
            case 'update' : return { renderDataMap -> render( template:"single.v1.0.xml",
                                                              model: [ foo: renderDataMap.data, refBase: refBase ] ) }

            default       : return null
        }

    }


    // --------------------------- Special 'params' handling & Rendering ---------------------------


    // We'll return a closure for any specific content type formats that we explicitly support.
    // If we don't return a closure, or if we don't even expose an 'extractParams' method at all,
    // the base class will check Spring for a registered handler class, and lastly will fall back to
    // it's default param extraction.
    //
    public Closure getParamsExtractor() {
        switch (request.getHeader( 'Content-Type' )) {
            case "application/vnd.sungardhe.student.v0.01+xml" : return handleXmlVer0_01
            default                                            : return null
        }
    }


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
    def handleXmlVer0_01 = { ->
        def xml = request.XML.Foo[0]
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
