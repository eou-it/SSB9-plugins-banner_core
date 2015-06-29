/*******************************************************************************
Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.testing.testing


/**
 * Controller supporting the 'Foo' model.
 **/
class FooController  {


    def fooService  // injected by Spring


    def invokedRenderCallbacks = [] // this is used for testing the framework -- it is NOT something controllers would normally have


    // A constructor is only needed when you need to explicitly set a domainClass or serviceName (see comments below).
    public FooRestfulController() {
        // domainClass = "Foo"        // explicitly set only when not following normal naming conventions for your controller
        // serviceName = "fooService" // explicitly set only when not following normal grails naming conventions
    }


    def index = {
        redirect( action: "view", params: params )
    }


    def view = {
        render "If I had a UI, I'd render it now!"
    }

}
