/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.testing


/**
 * Controller supporting the 'Foo' model.
 **/
class FooController  {


    def fooService  // injected by Spring


    def invokedRenderCallbacks = [] // this is used for testing the framework -- it is NOT something controllers would normally have

    def index = {
        redirect( action: "view", params: params )
    }


    def view = {
        render "If I had a UI, I'd render it now!"
    }

}
