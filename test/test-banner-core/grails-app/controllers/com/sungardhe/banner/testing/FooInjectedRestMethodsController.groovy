/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.exceptions.*

import grails.converters.JSON
import grails.converters.XML


/**
 * Controller supporting the Foo test model using injected RESTful CRUD methods.  
 **/
class FooInjectedRestMethodsController { 
    
    // This constrains the accepted HTTP methods.  // TODO: rename methods (see comments next to actions to be renamed)
    static allowedMethods = [ index: "GET", view: "GET", show: "GET", lookup: "GET", data: "GET", 
                              create: "POST", save: "POST", update: "PUT", remove: "DELETE" ]
    
    def defaultAction = 'list' 
    
    def fooService // injected by Spring


    // The 'message' tag library is available to all controllers and is accessible via a 'message' method.
    // Here we wrap this method as a closure, so that we can pass it as an argument.
    // Note that this particular controller exposes this publically, as it is used within the 
    // ApplicationExceptionIntegrationTests test. Normally, this closure could be private.
    // It is needed when asking a cought ApplicationException to prepare a localized resonse. 
    def localizer = { mapToLocalize ->
        message( mapToLocalize )
    }
    
    
    // in case someone uses a URI explicitly indicating 'index' and the URI mapping continues to support non-RESTful URIs
    def index = {
        redirect( action: "list", params: params ) // even with defaultAction, this redirect is still needed 
    }
                
    
    
    
    // If an exception occurs here, we won't really know how to handle... we'll let the errors controller deal with it.
    def list = { 
        params.max = Math.min( params.max ? params.max.toInteger() : 10, 100 ) 
        def results
        def totalCount
        if (params.query != null) {
            def q = "%" + params.query + "%"
            results = Foo.findAllByDescriptionIlikeOrCodeIlike( q, q )
            totalCount = Foo.countByDescriptionIlikeOrCodeIlike( q, q )
        } 
        else {
            results = fooService.list( params )
            totalCount = fooService.count()
        }
         
        withFormat {
            
            json {
                render( [ success: true, data: results, totalCount: totalCount ] as JSON )
            }
            
            xml {
                def refBase = "${request.scheme}://${request.serverName}:${request.serverPort}/${grailsApplication.metadata.'app.name'}/$controllerName"
                switch (request.getHeader( 'Content-Type' )) {
                    case 'application/vnd.sungardhe.student.v0.01+xml' :
                         response.setHeader( 'Content-Type', request.getHeader( 'Content-Type' ) )
                         render """<?xml version="1.0" encoding="UTF-8"?>
                                   <TestResponse>This pretend old version does not have any useful data!</TestResponse>
                                """
                         return
                    default :                
                         response.setHeader( "Content-Type", "text/xml" )
                         render( template:"list.v1.0.xml", model: [ fooList: results, totalCount: totalCount, refBase: refBase ] )
                }
            }
        } 
    }
            
    
    // TODO: Assess potential for code generation, given an XML Schema and a domain class
    // TODO: Attain this 'mapper' from Spring configuration.  Since our XML will adhere to other standards, we cannot
    // simply use the built-in Grails converters. Note that it is important to check for the existence of attributes and 
    // elements before adding to the map that will be used to create or modify the model.  This will allow us to 
    // accept partial XML fragments containing only the information needed to satisfy the request.  
    // For example, to update the foo description, the XML need only populate the description element. 
    // It is important that we do not add to the properties map any other properties (as doing so would set those  
    // property values to null when creating or updating a model instance.)
    private def paramsXml_v1_0( xml ) {
        def props = [:]
        if (xml.@id?.text())                     props.id              = xml.@id.toInteger()
        if (xml.@systemRequiredIndicator?.text()) props.systemRequiredIndicator = xml.@systemRequiredIndicator?.text()
        if (xml.@lastModifiedBy?.text())         props.lastModifiedBy  = "${xml.@lastModifiedBy.text()}"
        if (xml.@lastModified?.text())           props.lastModified    = xml.@lastModified.text()
        if (xml.@dataOrigin?.text())             props.dataOrigin      = "${xml.@dataOrigin.text()}"
        if (xml.@optimisticLockVersion?.text())  props.version         = xml.@optimisticLockVersion.toInteger()
                                                                      
        if (xml.Code?.text())                    props.code            = "${xml.Code.text()}"
        if (xml.Description?.text())             props.description     = "${xml.Description.text()}"
        
        if (xml.AddressStreetLine1?.text())      props.addressStreetLine1 = xml.AddressStreetLine1?.text()
        if (xml.AddressStreetLine2?.text())      props.addressStreetLine2 = xml.AddressStreetLine2?.text()
        if (xml.AddressStreetLine3?.text())      props.addressStreetLine3 = xml.AddressStreetLine3?.text()
        if (xml.AddressStreetLine4?.text())      props.addressStreetLine4 = xml.AddressStreetLine4?.text()
        if (xml.HouseNumber?.text())             props.houseNumber        = xml.HouseNumber?.text()
                                                
        if (xml.AddressCity?.text())             props.addressCity        = xml.AddressCity?.text()
        if (xml.AddressState?.text())            props.addressState       = xml.AddressState?.text()
        if (xml.AddressCountry?.text())          props.addressCountry     = xml.AddressCountry?.text()
        if (xml.AddressZipCode?.text())          props.addressZipCode     = xml.AddressZipCode?.text()
        
        if (xml.VoiceResponseMessageNumber?.text())    props.voiceResponseMessageNumber = xml.VoiceResponseMessageNumber?.text()
        if (xml.StatisticsCanadianInstitution?.text()) props.statisticsCanadianInstitution = xml.StatisticsCanadianInstitution?.text()
        if (xml.DistrictDivision?.text())              props.districtDivision = xml.DistrictDivision?.text()
        props
    }
        
}
