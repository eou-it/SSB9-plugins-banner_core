/* Copyright 2009-2013 Ellucian Company L.P. and its affiliates. */

modules = {
   'jqueryForManageBrowserInstance' {
       defaultBundle environment == "development" ? false : "jqueryForManageBrowserInstance"
       resource url:[plugin: 'banner-core', file: 'js/jquery/jquery-1.4.2.js'], disposition: 'head'
   }
}
