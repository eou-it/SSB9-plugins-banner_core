<!-- ********************************************************************
     Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************** -->

#Banner Core plugin documentation

##Status
Production quality, although subsequent changes may not be backward compatible.  Remember to include this software in export compliance reviews when shipping a solution that uses this plugin.

##Overview
This plugin adds Spring Security (aka Acegi) and a custom DataSource implementation (BannerDataSource) that together provide for authentication and authorization based upon Banner Security configuration. In addition, this plugin provides additional framework support (e.g., injecting CRUD methods into services, providing base test classes) to facilitate development of Banner web applications.

##Installation and quickstart
The recommended approach is to install the plugin as a git submodule.

###1. Add Git submodule
The plugin repo is located at ssh://git@devgit1/banner/plugins/banner_core.git and releases are tagged (e.g., 'pub-2.7.3'). We recommend using this plugin as an in-place plugin using Git submodules.

To add the plugin as a Git submodule under a 'plugins' directory:

        test_app (master)$ git submodule add ssh://git@devgit1/banner/plugins/banner_core.git plugins/banner_core.git
        Cloning into 'plugins/banner_core.git'...
        remote: Counting objects: 1585, done.
        remote: Compressing objects: 100% (925/925), done.
        remote: Total 1585 (delta 545), reused 309 (delta 72)
        Receiving objects: 100% (1585/1585), 294.45 KiB | 215 KiB/s, done.
        Resolving deltas: 100% (545/545), done.

Then add the in-place plugin definition to BuildConfig.groovy:

        grails.plugin.location.'banner-core' = "plugins/banner_core.git"

Note that adding the plugin this way will the latest commit on the master branch at the time you ran the submodule command.  If you want to use an official release instead, go to the plugin directory and checkout a specific version, e.g.:

    cd plugins/banner_core.git
    git checkout pub-2.7.3

Don't forget to go back to your project root and commit the change this will make to your git submodules file.


###2. Configure plugin dependencies
Irrespective of the method used to install the Banner Core plugin, the following changes must be made to include the plugin dependencies.  The plugin depends on spring-security-cas, banner-codenarc, and i18n-core plugins.

In the plugins section of BuildConfig.groovy add:

        grails.plugin.location.'spring-security-cas' = "../spring_security_cas.git"
        grails.plugin.location.'banner-codenarc'     = "../banner_codenarc.git"
        grails.plugin.location.'i18n-core'           = "../i18n_core.git"


###3. Configure the UrlMappings to use the controller
Edit the UrlMappings.groovy to look similar to the following defaults.  Your application map already have url mappings defined; if so, add the following mappings for as appropriate.

####For SSB apps:

    static mappings = {

        "/ssb/menu" {
            controller = "selfServiceMenu"
            action = [GET: "data", POST: "create"]
        }

        "/ssb/i18n/$name*.properties"(controller: "i18n", action: "index" )

        /login/auth" {
            controller = "login"
            action = "auth"
        }


        "/login/denied" {
            controller = "login"
            action = "denied"
        }

        "/login/authAjax" {
            controller = "login"
            action = "authAjax"
        }

        "/login/authfail" {
            controller = "login"
            action = "authfail"
        }

        "/logout" {
            controller = "logout"
            action = "index"
        }

        "/logout/timeout" {
            controller = "logout"
            action = "timeout"
        }

        "/login/resetPassword" {
            controller = "login"
            action = "forgotpassword"
        }


        "/resetPassword/validateans" {
            controller = "resetPassword"
            action = "validateAnswer"
        }


        "/resetPassword/resetpin" {
            controller = "resetPassword"
            action = "resetPin"
        }


        "/resetPassword/auth" {
            controller = "login"
            action = "auth"
        }


        "/ssb/resetPassword/auth" {
            controller = "login"
            action = "auth"
        }


        "/resetPassword/recovery" {
            controller = "resetPassword"
            action = "recovery"
        }


        "/resetPassword/validateCode" {
            controller = "resetPassword"
            action = "validateCode"
        }


        "/resetPassword/login/auth" {
            controller = "login"
            action = "auth"
        }


        "/resetPassword/logout/timeout" {
            controller = "logout"
            action = "timeout"
        }
    }

#####For Admin apps:

    static mappings = {

        // Special validation URIs

        "/$domainName/validation/" {
            controller = "validation"
            action = [GET: "data"]
        }
        "/$domainName/validation/view" {
            controller = "validation"
            action = [GET: "view"]
        }
        "/$domainName/validation/delete/$id" {
            controller = "validation"
            action = [DELETE: "delete"]
        }
        "/$domainName/validation/create" {
            controller = "validation"
            action = [POST: "create"]
        }
        "/$domainName/validation/update/$id" {
            controller = "validation"
            action = [PUT: "update"]
        }


        // Deprecated 'non-RESTful' mappings

        "/$controller" {
            action = [GET: "data", POST: "create"]
        }

        "/$controller/$action" { }
        
        "/$controller/$action/$id" { }

       // 'special' mappings --
        // note 'interest' uses a specific mapping that is not usually needed or desired -- see 'normal' RESTful mapping below...
        "/api/interests"(controller: "interestRest") {
            action = [GET: "list", POST: "save"]
        }

        "/api/interest/$id"(controller: "interestRest") {
            action = [GET: "show", PUT: "update", DELETE: "delete"]
        }

        // 'normal' RESTful mappings, for when the controller can be determined by the URI

        "/api/$controller" { // note we don't use ( parseRequest:true ) as we'll parse manually via 'request.XML'
            action = [ GET: "list", POST: "create" ]
        }

        "/api/$controller/$id" { // note we don't use ( parseRequest:true ) as we'll parse manually via 'request.XML'
            action = [ GET: "show", PUT: "update", DELETE: "destroy" ]
        }

      "/"(view:"/index")
        "500"(controller: "error", action: "internalServerError")
        "403"(controller: "error", action: "accessForbidden")

        //exception for this view
        "/$controller/index"(view:'index')

        "/index.gsp"(view:"/index")
    }


###4. Configure the non-caching DB connection details

Add the following to Config.groovy

/ ******************************************************************************
// DB Connection Caching Configuration
// ******************************************************************************
// Note: The BannerDS will cache database connections for administrative users,
// however for RESTful APIs we do not want this behavior (even when
// authenticated as an 'administrative' user). RESTful APIs should be stateless.
//
// IMPORTANT:
// When exposing RESTful endpoints, exclude database caching for those URLs.
// Also, if using a prefix other than 'api' and 'qapi' you will need to ensure
// the spring security filter chain is configured to avoid creating a session.
//
avoidSessionsFor = [ 'api', 'qapi' ]

