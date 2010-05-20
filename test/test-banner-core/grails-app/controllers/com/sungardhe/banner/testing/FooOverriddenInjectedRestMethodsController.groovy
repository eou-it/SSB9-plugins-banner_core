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
 * Controller supporting the Foo test model using injected RESTful CRUD methods and 
 * that overrides both request parsing (into the params map) and rendering.  This 
 * controller supports custom XML rendering, that adheres to an XML Schema. 
 * @See com.sungardhe.banner.controllers.DefaultRestfulControllerMethods comments for
 * a discussion on the 'override' methods.
 **/
class FooOverriddenInjectedRestMethodsController { 
    
    static defaultCrudActions = true // injects save, update, delete, show, and list actions
    
    static allowedMethods = [ index: "GET", view: "GET",                                                 // --> allow non-RESTful,
                              show: "GET", list: "GET", save: "POST", update: "PUT", remove: "DELETE" ]  // --> ensure RESTful
               
    def fooService  // injected by Spring
    
    
    // in case someone uses a URI explicitly indicating 'index' or our URI mapping includes a non-RESTful mapping to 'index'
    def index = {
        redirect( action: "view", params: params )  
    }
    
    
    // Render main User Interface page -- note that ALL other actions are injected :-)
    def view = {
        render "If I had a UI, I'd render it now!"
        // Render the main ZUL page supporting this model.  All subseqent requests from this UI will be 
        // handled by the corresponding 'Composer'.  This is the +only+ action supporting the ZK based user interface.
        // The other actions in this controller support RESTful clients. 
    }
    
    
    def extractParams() {
        switch (request.header["Content-Type"]) {
            case "application/vnd.sungardhe.student.v0.01+xml" :
                // we'll only add specific support for a custom MIME type
                def handleXmlVer0_01 = {
                    paramsXml_v1_0( request )
                }
                return handleXmlVer0_01
            default:
                return null // the injected action should use rely on default extraction
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
    def paramsXml_v1_0( xml ) {
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
