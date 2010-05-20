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
 * See 'FooOverridenInjectedMethodsController' for usage of injected methods while having control
 * over 'success' rendering and param map extraction.
 **/
class FooInjectedRestMethodsController { 
    
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
                
                
        
}
