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
 * Controller supporting the Foo test model using injected RESTful CRUD methods.  
 * See 'FooOverriddenInjectedMethodsController' for usage of injected methods while having control
 * over 'success' rendering and params map extraction.  Note that the actions used
 * to expose a RESTful interface are provided by the RestfulControllerMixin mixin.
 **/
@Mixin(RestfulControllerMixin) // Note: Also needs 'hasRestActions' property (see below)
class FooRestfulController  {

    // HACK -- This property facilitates identifying this controller as having mixed-in REST actions.  It is needed since
    //         the Mixin annotation is not available at runtime, and we need to discover these controllers during bootstrap
    //         in order to register these REST actions so the URI mapping to them succeeds.
    boolean hasRestMixin = true

    def fooService  // injected by Spring
    
    
    public FooRestfulController() {
        // NOTE: domainSimpleName and serviceName properties are provided by the mixin.  Since this controller does not
        // follow normal naming conventions (i.e., there is no model named 'FooRestful'), we need this constructor
        // in order to explicitly set the domainSimpleName.
        domainSimpleName = "Foo"

        // Similarly, we must explicitly set serviceName if it cannot be derived from the domainSimpleName using normal conventions.
        // In this case, since we do follow normal conventions for services (based on the domainSimpleName), we don't need to
        // explicitly set this here. 
        // serviceName = "fooService"
    }
    
    
    // ---------------------------- User Interface Actions (non-RESTful) ----------------------------------
    // The following actions are here solely to illustrate that additional actions can be implemented here.
    // In general, our controllers will support only RESTful clients for integration purposes, and will
    // not have non-RESTful actions like 'index' or 'view'.
    
    
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
                
                
        
}
