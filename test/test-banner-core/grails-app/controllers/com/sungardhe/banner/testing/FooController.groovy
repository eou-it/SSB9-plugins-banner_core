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
 * Controller supporting the Foo model, that is used to test the framework.  
 **/
class FooController { 
    
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
    public def localizer = { mapToLocalize ->
        message( mapToLocalize )
    }


    // in case someone uses a URI explicitly indicating 'index' and the URI mapping continues to support non-RESTful URIs
    def index = {
        redirect( action: "list", params: params ) // even with defaultAction, this redirect is still needed 
    }
        
    
    def save = {
        withFormat {

            html {
                def foo = new Foo( params )
                try {
                    def fooInstance = fooService.create( foo )
                    flash.message = "${ message( code: 'default.created.message', args: [ message( code: 'foo.label', default: 'Foo'), fooInstance.id])}"
                    redirect( action: "show", id: fooInstance.id )
                }
                catch (ApplicationException e) { 
                    def resultMap = e.returnMap( localizer )
                    flash.message = resultMap.message
                    flash.errors = resultMap.errors
                    render( view: "create", model: [ fooInstance: foo ] )
                } catch (e) { // CI logging
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    flash.message = "${ message( code: 'default.not.created.message', args: [ message( code: 'foo.label', default: 'Foo') ] )}"
                    flash.errors = e.message
                    render( view: "create", model: [ fooInstance: foo ] )                    
                }
            }            
            
            json {
                response.setHeader( "Content-Type", "application/json" ) 
                def foo
                try {
                    foo = new Foo( JSON.parse( params?.data ) )
                    if (foo.id) {
                        // We'll assume existance of an id means this entity is already persisted, and we need to do an update
                        // This 'breaks' REST conventions, but we'll do this versus failing by trying to add a duplicate model instance.
                        update.call()
                    } 
                    else {
                        foo = fooService.create( foo )
                        render( [ success: true, 
                                  data: foo, 
                                  message: "${message( code: 'default.created.message', \
                                           args: [ message( code: 'foo.label', default: 'Foo'), foo.code ] )}" ] as JSON )
                    }
                } 
                catch (ApplicationException e) {
println "XXXXXXXXXXXXXXXXXXXXX AE: $e"
                    response.setStatus( e.getHttpStatusCode() ) 
                    render( (e.returnMap( localizer ) + [ data: foo ]) as JSON ) // exception already logged within service
                } 
                catch (e) { // CI logging
println "XXXXXXXXXXXXXXXXXXXXX E: $e"
                    response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    render( [ data: foo,
                              success: false, 
                              message: "${message(code: 'default.not.created.message', \
                                        args: [ message( code: 'foo.label', default: 'Foo') ] )}",
                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { message( error: it ) } : null),
                              underlyingErrorMessage: e.message ] as JSON )
                }
            }
            
            xml {
                response.setHeader( "Content-Type", "text/xml" ) 
                def refBase = "${request.scheme}://${request.serverName}:${request.serverPort}/${grailsApplication.metadata.'app.name'}/$controllerName"
                try {
                    // Note: If this controller did NOT support XML, we could just create a runtime exception and wrap it in ApplicationException
                    // so that it gets caught below.  Runtime exceptions, when wrapped inside an ApplicationException, can contain resource codes 
                    // and parameters in the following form: '@@r1:{resourceCode}:{param1}:{param2}@@'. Here's an example of what could be thrown here:
                    //
                    // throw new ApplicationException( Foo, new RuntimeException( "@@r1:runtime.not.yet.implemented:FooController:.create@@" )) 
                    
                    def xml = request.XML
                    def props = paramsXml_v1_0( xml.Foo[0] )
                    log.debug "FooController.create() has extracted from the XML request, the following props: $props along with params = $params"
                    foo = fooService.create( props )
                    response.setHeader( "Content-Type", "text/xml" )
                    render( template:"single.v1.0.xml", model: [ foo: foo, refBase: refBase ] )
                } 
                catch (ApplicationException e) {
                    response.setStatus( e.getHttpStatusCode() ) 
                    render( e.returnMap( localizer ) as XML )  // exception already logged within service
                } 
                catch (e) { // CI logging
                    response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    render( [ success: false, 
                              message: "${message(code: 'default.not.created.message', \
                                        args: [ message( code: 'foo.label', default: 'Foo') ] )}",
                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { message( error: it ) } : null),
                              underlyingErrorMessage: e.message ] as XML )          
                }
            }
        }
    }


    def update = {
        withFormat {
            
            json {  
                response.setHeader( "Content-Type", "application/json" ) 
                try {
                    def foo = fooService.update( JSON.parse( params?.data ) ) 
                    render( [ success: true, data: foo, 
                              message: "${message( code: 'default.created.message', \
                                        args: [ message( code: 'foo.label', default: 'Foo'), foo.code ] )}" ] as JSON )
                } 
                catch (ApplicationException e) {
                    response.setStatus( e.getHttpStatusCode() ) 
                    render( e.returnMap( localizer ) as JSON ) // exception already logged within service
                } 
                catch (e) { // CI logging
                     response.setStatus( 500 ) 
                     log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                     render( [ success: false, 
                               message: "${message(code: 'default.not.updated.message', \
                                        args: [ message( code: 'foo.label', default: 'Foo') ] )}",
                               errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { message( error: it ) } : null),
                               underlyingErrorMessage: e.message ] as JSON )                 
                }
            }
            
            xml {
                response.setHeader( "Content-Type", "text/xml" ) 
                def refBase = "${request.scheme}://${request.serverName}:${request.serverPort}/${grailsApplication.metadata.'app.name'}/$controllerName"
                try {
                    response.setHeader( "Content-Type", "text/xml" )
                    def xml = request.XML
                    def props = paramsXml_v1_0( xml.Foo[0] )
                    log.debug "FooController.update() has extracted from the XML request, the following props: $props along with params = $params"
                    
                    def foo = fooService.update( props ) 
                    render( template:"single.v1.0.xml", model: [ foo: foo, refBase: refBase ] )
                } 
                catch (ApplicationException e) {                
                    response.setStatus( e.getHttpStatusCode() ) 
                    render( e.returnMap( localizer ) as XML ) // exception already logged within service
                } 
                catch (e) { // CI logging
                    response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    render( [ success: false, 
                              message: "${message(code: 'default.not.updated.message', \
                                       args: [ message( code: 'foo.label', default: 'Foo') ] )}",
                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { message( error: it ) } : null),
                              underlyingErrorMessage: e.message ] as XML )
                }
            }
        }
    }
    

    def delete = {
        withFormat {
            
            json {        
                response.setHeader( "Content-Type", "application/json" ) 
                try {
                    fooService.delete( params.id )
                    render ( [ success: true ] as JSON )
                } 
                catch (ApplicationException e) {
                    response.setStatus( e.getHttpStatusCode() ) 
                    render( ((ApplicationException) e).returnMap( localizer ) as JSON ) // exception already logged within service
                } 
                catch (e) { // CI logging
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    response.setStatus( 500 ) 
                    render( [ success: false, 
                              message: "${message(code: 'default.not.deleted.message')}", 
                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { message( error: it ) } : null),
                              underlyingErrorMessage: e.message ] as JSON )
                }
            }
            
            xml {
                try {
                    response.setHeader( "Content-Type", "text/xml" )
                    fooService.delete( params.id )
                    render ( [ success: true ] as XML )
                } 
                catch (ApplicationException e) {
                    response.setStatus( e.getHttpStatusCode() ) 
                    render( e.returnMap( localizer ) as XML ) 
                } 
                catch (e) { // CI logging
                    response.setStatus( 500 )
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    render( [ success: false, 
                              message: "${message(code: 'default.not.deleted.message')}", 
                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { message( error: it ) } : null),
                              underlyingErrorMessage: e.message ] as XML )
                }
            }
        }
    }
    
    
    def show = {
        withFormat {
            
            html {
                try {
                    def foo = fooService.read( params.id )
                    [ fooInstance: foo ]
                }
                catch (ApplicationException e) { 
                    def resultMap = e.returnMap( localizer )
                    flash.message = resultMap.message
                    flash.errors = resultMap.errors
                    render( view: "show", fooInstance: foo )
                } catch (e) { // CI logging
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    flash.message = "${ message( code: 'default.not.created.message', args: [ message( code: 'foo.label', default: 'Foo') ] )}"
                    flash.errors = e.message
                    render( view: "show", fooInstance: foo )                    
                }
            }            
            
            json {        
                try {
                    response.setHeader( "Content-Type", "application/json" ) 
                    def result = fooService.read( params.id )
                    render( [ success: true, data: result ] as JSON )
                } 
                catch (ApplicationException e) {                    
                    response.setStatus( e.getHttpStatusCode() ) 
                    render( e.returnMap( localizer ) as JSON ) // exception already logged within service
                } 
                catch (e) { // CI logging
                    response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    render( [ success: false, 
                              message: "${message(code: 'default.internal.error')}", 
                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { message( error: it ) } : null),
                              underlyingErrorMessage: e.message ] as JSON )
                }
            }
            
            xml {
                def refBase = "${request.scheme}://${request.serverName}:${request.serverPort}/${grailsApplication.metadata.'app.name'}/$controllerName"
                try {
                    def result = fooService.read( params.id )
                    response.setHeader( "Content-Type", "text/xml" )
                    render( template:"single.v1.0.xml", model: [ foo: result, refBase: refBase ] )
                } 
                catch (ApplicationException e) {                    
                    response.setStatus( e.getHttpStatusCode() ) 
                    render( e.returnMap( localizer ) as XML ) 
                } 
                catch (e) { // CI logging
                    response.setStatus( 500 )
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    render( [ success: false, 
                              message: "${message(code: 'default.internal.error')}", 
                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { message( error: it ) } : null),
                              underlyingErrorMessage: e.message ] as XML )
                }
            }
        }
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
