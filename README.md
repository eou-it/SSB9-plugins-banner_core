<!-- ********************************************************************
     Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************** -->

#Banner Core plugin documentation

####Status
Production quality, although subsequent changes may not be backward compatible.  Remember to include this software in export compliance reviews when shipping a solution that uses this plugin.

####Overview
This plugin adds Spring Security (aka Acegi) and a custom DataSource implementation (BannerDataSource) that together provide for authentication and authorization based upon Banner Security configuration. This plugin also provides additional framework support (e.g., injecting CRUD methods into services, providing base test classes) to facilitate development of Banner web applications. Lastly, this plugin provides additional Banner 'general' support including Supplemental Data Engine (SDE) and Multi-Entity Processing (MEP).

Key features provided by the plugin include:

* Configuring Spring Security for authentication (form-based, CAS, external) and authorization (based upon the existing Banner 8 security model)
* A specialized 'Datasource' that proxies connections for the logged in user, so that Oracle FGAC remains effective. Administrative user connections are cached for the duration of the user's session.
* A filter that sets a 'FormContext' corresponding to a legacy Banner Form, based upon the URI requested and a 'formControllerMap' witin Config.groovy.  This is used to so that only the applicable roles are unlocked on the connection.
* A special 'ROLE_DETERMINED_DYNAMICALLY' role that grants access if the logged in user has any roles (other than those ending with '_Q' and '_CONNECT') that pertain to the FormContext associated to the URI
* A base class for services that provides default CRUD implementations.
* Base classes for integration and functional tests that facilitate testing of exceptions, logging in and out, etc.

#Installation and quickstart
The recommended approach is to install the plugin as a git submodule.

###Add Git submodule
To add the plugin as a Git submodule under a 'plugins' directory:

        $ git submodule add ssh://git@devgit1/banner/plugins/banner_core.git plugins/banner_core.git
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

Don't forget to go back to your project root and commit the change, as this will establish your project's git submodule dependency to the desired commit of the plugin.


###2. Configure plugin dependencies
The plugin depends on spring-security-cas, banner-codenarc, and i18n-core plugins.  These dependencies are configured in BuildConfig.groovy and are expected to be 'sibling' git submodule dependencies for your project.  That is, it is the containing project's responsibility to ensure these plugin dependencies are available on the file system.  If you adopt a different approach, you will need to modify the location of these dependencies by editing BuildConfig.groovy:

        grails.plugin.location.'spring-security-cas' = "../spring_security_cas.git"
        grails.plugin.location.'banner-codenarc'     = "../banner_codenarc.git"
        grails.plugin.location.'i18n-core'           = "../i18n_core.git"


###Configure UrlMappings
Edit the UrlMappings.groovy to include appropriate mappings.  The below mappings may be considered:

#####For SSB apps:
The mappings below are usually added to support Banner XE self-service applications.

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


#####For Admin apps:
The mappings below are usually added to support Banner XE administrative applications.

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

    "/$controller/$action" { }

    "/$controller/$action/$id" { }

    "/"(view:"/index")
    "500"(controller: "error", action: "internalServerError")
    "403"(controller: "error", action: "accessForbidden")

    //exception for this view
    "/$controller/index"(view:'index')

    "/index.gsp"(view:"/index")

#####For RESTful APIs:
The mappings below are usually added alongside those supporting admin and or SSB, when exposing RESTful APIs using the 'restful-api' Grails plugin. Note that support for these mappings requires both the restful-api plugin and the banner-restful-api-support plugins to be added as Git submodules to your project. _(These mappings are not supported directly by banner-core, but are included here as incorporating RESTful APIs is common across Banner XE applications.)_

    // ------------------- RESTful API end points --------------------

    // The normal URL mappings used for resources supported by the
    // restful-api plugin.
    //
    "/api/$pluralizedResourceName/$id"(controller:'restfulApi') {
        action = [GET: "show", PUT: "update", DELETE: "delete"]
        parseRequest = false
        constraints {
            // to constrain the id to numeric, uncomment the following:
            // id matches: /\d+/
        }
    }

    "/api/$pluralizedResourceName"(controller:'restfulApi') {
        action = [GET: "list", POST: "create"]
        parseRequest = false
    }

    // Support for nested resources. You may add additional URL mappings to handle
    // additional nested resource requirements.
    //
    "/api/$parentPluralizedResourceName/$parentId/$pluralizedResourceName/$id"(controller:'restfulApi') {
        action = [GET: "show", PUT: "update", DELETE: "delete"]
        parseRequest = false
    }

    "/api/$parentPluralizedResourceName/$parentId/$pluralizedResourceName"(controller:'restfulApi') {
        action = [GET: "list", POST: "save"]
        parseRequest = false
    }

    // We'll also expose URLs using a different prefix to support querying with POST.
    //
    "/qapi/$pluralizedResourceName"(controller:'restfulApi') {
        action = [GET: "list", POST: "list"]
        parseRequest = false
    }

    // Also support mepCode identification before /api/
    //
    "/$mepCode/api/$pluralizedResourceName/$id"(controller:'restfulApi') {
        action = [GET: "show", PUT: "update", DELETE: "delete"]
        parseRequest = false
    }

    "/$mepCode/api/$pluralizedResourceName"(controller:'restfulApi') {
        action = [GET: "list", POST: "create"]
        parseRequest = false
    }

    "/$mepCode/api/$parentPluralizedResourceName/$parentId/$pluralizedResourceName/$id"(controller:'restfulApi') {
        action = [GET: "show", PUT: "update", DELETE: "delete"]
        parseRequest = false
    }

    "/$mepCode/api/$parentPluralizedResourceName/$parentId/$pluralizedResourceName"(controller:'restfulApi') {
        action = [GET: "list", POST: "save"]
        parseRequest = false
    }

###Configure Security

Grails projects using this plugin must also use Spring Security.  This plugin leverages existing Banner Security (e.g., groups and roles) to reduce the impact of adopting Banner XE solutions. This plugin supports form-based authentication (i.e., a login form), CAS SSO, and SAML 2 based SSO.

#####High level Overview of Security

Following are the security-related steps that occur (when SSO is not being employed) during a request:

1. A user attempts to reach a URL such as http://myschool.edu/banner/basicCourceInformation
1. The user is redirected to a login page if not already authenticated.
1. The user is authenticated by connecting to the database using the supplied credentials.  This is performed within the 'BannerAuthenticationProvider'. If the provider is able to establish a connection to the Banner database, the user is 'authenticated'.  Before returning, the provider will establish the user's authorities.
1. The provider retrieves the effective Banner security roles for that user, using a database view. The Banner security role assignments for this user are then used to construct 'security roles' in a format usable by Spring Security. For instance, if a user has been given the BAN_DEFAULT_Q role for object 'FPARORD' and the BAN_DEFAULT_M role for object 'STVCOLL', that user will be granted two 'authorities' named "ROLE_FPARORD_BAN_DEFAULT_Q" and "ROLE_STVCOLL_BAN_DEFAULT_M".
1. The user is redirected back to their desired URI once authentication completes, or is sent back to the login screen if authentication fails.
1. A filter intercepts the request and verifies that the user is authenticated (which he/she is, as we just did that\!), and determines if the user is authorized to reach the composer or controller.  This authorization is based upon the 'interceptUrlMap' map (to be discussed below) within Config.groovy that relates each URL to authentication and authorization requirements. Please see below for details on how this map can be used.
1. Once we have authenticated and authorized the user, a 'FormContext' threadlocal is set based upon a 'formControllerMap' (to be discussed below) contained in Config.groovy that maps controller and composer names to one or more 'Banner Classic Forms'.  When a controller is being accessed, this is performed by a 'before filter' (really an AOP Aspect) that intercepts the request.  When a composer is being accessed, this is handled by a custom ZK page filter (that is explicitly wired into the ZK plugin).  For instance, if the user is navigating to a "/college" URL, the filter will set a FormContext value of 'STVCOLL' (assuming the formControllerMap relates the College controller and or composer with 'STVCOLL').  Controllers may map to more than one legacy Oracle object.
1. The controller or composer mapped to the URL is then allowed to service the request. To do this, the controller or composer interacts with transactional services and domain models.  Whenever a database connection is needed (either indirectly by the Hibernate ORM library or used explicitly), it is retrieved from the 'BannerDS' dataSource.  This dataSource proxies the connection on behalf of the logged-in user, calculates the 'applicable' authorities for that user (i.e., retrieves the authorities from the authentication object that pertain to the Banner object being accessed, as identified in the FormContext), and unlocks those roles.
1. When the transaction is committed (declaratively), the connection proxy session is closed and the connection is returned to the pool.  The form context is also cleared using an 'after' filter.

The Spring Security Core Grails plugin being used provides a number of convenience features, including tags that can be used within the view to identify the user, verify the user has certain roles, etc.  Details are available [here|http://burtbeckwith.github.com/grails-spring-security-core/docs/manual/index.html].

**Configuration**

Currently, there are two important maps that must reside within the Config.groovy of any project using this plugin. These maps must be updated when introducing new controllers or composers.

Config.groovy must contain a 'formControllerMap' map, similar to the one depicted below.  This map relates controller and composer names to the Banner 8 objects (e.g., Forms) for which they correspond.  Note in most cases there will be a one-to-one mapping, but it is possible that one Grails controller or composer is being used to replace multiple Banner objects.  This map allows for grails applications to leverage existing Banner security configuration.

```groovy
// ******************************************************************************
//          +++ SECURITY: FORM-CONTROLLER MAP & INTERCEPT URL MAP +++
// ******************************************************************************
// This map relates controllers to the Banner forms that it replaces.  This map
// supports 1:1 and 1:M (where a controller supports the functionality of more than
// one Banner form.  This map is critical, as it is used by the security framework to
// set appropriate Banner security role(s) on a database connection. For example, if a
// logged in user navigates to the 'medicalInformation' controller, when a database
// connection is attained and the user has the necessary role, the role is enabled
// for that user and Banner object.  In addition, this information 'may' be used
// when determining if a user is authorized to access the endpoint (see 'interceptUrlMap'
// comments below).
formControllerMap = [
                      'college'            : [ 'STVCOLL' ],
                      'interest'           : [ 'STVINTS' ],
                      'medicalInformation' : [ 'GOAMEDI' ],
                      ...
                    ]
```

The above 'formControllerMap' is used by a 'before filter' (AccessControlFilters.groovy) to set a ThreadLocal with the Banner 8 object name(s) corresponding to the controller or composer being accessed.  The 'FormContext' threadlocal is then used by the security framework when determining whether the user can access the URL mapped to the controller or composer. As described above, this is done by checking the user's 'Banner role assignments' that were cached during the authentication process.

Whether or not a user is allowed to access a URL depends upon a Spring Security Map that defines the roles needed to access each URL.  This map must also be included within Config.groovy, and represents a standard 'acegi' configuration:

```groovy
// The following map is used to secure URLs, based upon authentication or role-based authorization.
// In general, users should be granted access to Banner pages if they have any roles that pertain to
// the corresponding Banner Form/Object (except if their only applicable role ends with '_CONNECT').
// Please see comments below regarding the special 'ROLE_DETERMINED_DYNAMICALLY' role.
grails.plugins.springsecurity.interceptUrlMap = [
        '/'          : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/zkau/**'   : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/zkau**'    : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/login/**'  : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/mainPage**': ['ROLE_SCACRSE_BAN_DEFAULT_M'], // not in formControllerMap, so we must explicitly identify a role
        '/menu/**'   : ['ROLE_SCACRSE_BAN_DEFAULT_M'], // not in formControllerMap, so we must explicitly identify a role
        '/index**'   : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/logout/**' : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/js/**'     : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/css/**'    : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/images/**' : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/plugins/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/errors/**' : ['IS_AUTHENTICATED_ANONYMOUSLY'],

        // ALL URIs specified with the BannerAccessDecisionVoter.ROLE_DETERMINED_DYNAMICALLY
        // 'role' (it's not a real role) will result in authorization being determined based
        // upon a user's role assignments to the corresponding form (see 'formControllerMap' above).
        // Note: This 'dynamic form-based authorization' is performed by the BannerAccessDecisionVoter
        // registered as the 'roleVoter' within Spring Security.
        //
        // Only '/name_used_in_formControllerMap/' and '/api/name_used_in_formControllerMap/'
        // URL formats are supported.  That is, the name_used_in_formControllerMap must be first, or
        // immediately after 'api' -- but it cannot be otherwise nested. URIs may be protected
        // by explicitly specifying true roles instead -- as long as ROLE_DETERMINED_DYNAMICALLY
        // is NOT specified.
        //
       '/**': [ 'ROLE_DETERMINED_DYNAMICALLY' ]
]
```

As described in the code comments above, URLs may be protected with a special 'ROLE_DETERMINED_DYNAMICALLY' role.  A custom 'role voter' is configured by banner-core that will, when a URL is protected by this special role, determine authorization based upon the formControllerMap. Specifically, if a user has ANY roles corresponding to the Banner Classic Form identified in the formControllerMap for the current request, the user is granted access. (Note: Any roles ending with '_CONNECT' are excluded, and are not used to grant access.)

####Configuring RESTful API Support
RESTful API support requires the use of the open source '[restful-api](https://github.com/restfulapi/restful-api)' plugin developed by Ellucian.  Please see the restful-api plugin's [README](https://github.com/restfulapi/restful-api/blob/master/README.md) for general intallation and configuration instructions.

In addition, when using the restful-api plugin along with banner-core, the 'banner-restful-api-support' plugin should also be installed as an in-place plugin (and Git submodule) of the application. Please see the banner-restful-api-support plugin's README.md for additional configuration instructions.

####Self Service Banner Security
banner-core blurs the lines somewhat with respect to administrative and self service Banner.  An application using banner-core may be configured to support either administrative users or self service users, or both, within the same instance.

To enable self service, you must set the following configuration.

```groovy
/* ***************************************************************************
 *               Self Service Endpoint Support Enablement                    *
 *************************************************************************** */

// Set 'ssbEnabled' to true for instances that expose Self Service Banner endpoints.
// If this is set to false, or if this configuration item is missing, the instance
// will only support administrative users and not self service users.
//
// If this is enabled, it is important to also ensure the corresponding configuration
// items for the SSB datasource are configured below.
//
ssbEnabled = true                     // default is 'false'
//administrativeBannerEnabled = true  // default is 'true'

// Set 'ssbOracleUsersProxied = true' to ensure that database connections are proxied
// when the user has an oracle account.  This allows FGAC even for SSB pages.
// Set this to false to instead use database connections that are established
// for SSB users who do not have Oracle database accounts.  This setting applies
// only to SSB pages.
ssbOracleUsersProxied = true
```

The above configuration enables self service support, and also configures the solution to proxy database connections for users who have an Oracle database account.  Note that this configuration also allows 'administrative' authentication to be disabled, meaning that users would have to authenticate using spriden_id/pin or employ SSO.  Since administrative support is 'true' by default, this configuration is only needed when setting this to 'false'.

When both self service and administrative Banner are enabled, a SelfServiceBannerAuthenticationProvider is wired into the Spring Security framework.  This provider will attempt to authenticate the user using spriden_id and pin.  If this authentication fails (e.g., because the user does not have a PIDM), then the administrative 'BannerAuthenticationProvider' will attempt authentication based upon Oracle username and password.

It is important to note that when self service is enabled, a second dataSource must be configured to support self service users as shown below:

```groovy
/* ***************************************************************************
 *                Self Service User DataSource Configuration                 *
 *************************************************************************** */

// It is imperative the self-service datasource be configured if 'ssbEnabled = true'.

// JNDI configuration for use in 'production' environment
//
bannerSsbDataSource.jndiName = "jdbc/horizonDataSource"

// Local configuration for use in 'development' and 'test' environments
//
bannerSsbDataSource.url      = "jdbc:oracle:thin:@oracledb:1521:ban83"
bannerSsbDataSource.username = "baninst1"
bannerSsbDataSource.password = "baninst1_password"
bannerSsbDataSource.driver   = "oracle.jdbc.OracleDriver"
```

Note the above configuration is not needed for instances where self service is not enabled.

If the SelfServiceAuthenticationProvider is able to authenticate the user, it will then query the Banner database for the user's web roles. These are converted into Spring Security roles much like the Banner administrative roles.  In addition, if the user has an Oracle database account, the user's Banner administrative roles will also be retrieved and converted to Spring Security roles. In addition,  a special role will be given to the user that will allow access to self service pages and that will unlock the BAN_DEFAULT_ROLE if database connections are proxied.

##Services
The banner-core plugin provides a 'ServiceBase' base class for transactional services that supports the standard CRUD methods.

#####CRUD Method Input Arguments

ServiceBase provides 'create', 'update', 'delete', 'createOrUpdate', 'fetch', and 'list' methods. To facilitate reuse, these methods support various inputs.

The 'create' and 'update' methods accept a single argument, being either:

* An instance of a domain model
* A 'params' Map (i.e., a map holding property key-value entries, such as a normal Grails 'params' map)
* A Map containing a key named after the model's class (in property form), whose instance is held by that key. For instance, to hold an instance of a MyModel model class, you may use a key named 'myModel'.
* A Map that contains a key named 'domainModel', whose value is a domain model instance
* A List that contains any combination of the above. When providing a list, the create/update method will be invoked separately for each element in the list. That is, it does not implement 'true batch processing' but instead provides an 'internal iterator' for convenience.

Similarly, the delete method accepts a single argument, being either:

* An 'id' (either as a 'Long', primitive 'long', or String). If a String representation is provided, it will be coerced into a long if possible otherwise it will be used as a string. In general, we do not have complete support for String primary keys, so in practice a 'long' compatible key should be used.
* An instance of a domain model that has a populated id
* A 'params' map (i.e., a map holding property key-value entries, such as a normal Grails 'params' map)
* A Map containing a key named for the model (in property form) whose instance is held by that key. For instance, to hold an instance of a MyModel model class, you may use a key named 'myModel'.
* A map that contains a 'domainModel' key whose value is a domain model instance
* A List that contains any combination of the above. When providing a list, the create method will be invoked separately for each element in the list. That is, it does not implement 'true batch processing' but instead provides an 'internal iterator' for convenience.

The delete method returns 'true' if the delete was successful, or an exception if it was not successful.

The 'list' and 'read' (as well as a 'get' method providing the same implementation as 'read' under a different name) simply pass along any input arguments to the corresponding GORM 'list' and 'get' methods (e.g., to support paging and filtering).

#####CRUD Method Hook Methods

The 'create', 'update', and 'delete' methods each invoke 'pre' and 'post' methods (e.g., preCreate, postUpdate, etc.) if implemented within the concrete service.  The 'pre' methods are invoked with the same input argument of the create/update/delete method invocation, and the 'post' invocations are provided a map containing status and results of the database operation.

While the 'pre' callback argument is whatever was passed to 'create', the 'post' callback arguments differ slightly for 'create', 'update', and 'delete'.  The 'postCreate' callback is invoked with a single argument: '\[ before: domainModelOrMap, after: createdModel \]' where 'domainModelOrMap' is whatever was passed into the create method, and 'createdModel' is the as-created model instance.  The 'postUpdate' argument is the same, except instead of a 'createdModel' key it contains an 'updatedModel' key. Lastly, the 'postDelete' callback argument looks like: '\[ before: domainObject, after: null \]' where 'domainObject' is the model instance that was fetched from the database and that will be deleted. The 'after' key simply holds null, as the model instance has been deleted.

#####CRUD Method Return Values
The create and update methods return the created/updated model instance if successful, and the 'delete' method returns 'true' if successful.  If any of these method invocations is not successful, they throw an exception.  Usually, the exception to be thrown will be an 'ApplicationException', however it is prudent to write your client that is invoking a service method to first catch ApplicationException (and handle it) and then in a second catch block catch 'Exception' (or Throwable).  This second catch should attempt to handle the exception, and should also log this exception as an 'error' that  should not have been thrown by the service. That is, log an error to indicate that the service should have wrapped this exception into an ApplicationException.

#####Application Exception

The banner-core plugin defines an ApplicationException that is used to wrap various exceptions, provide a common interface, and ensure localization of exception messages. Please refer to 'ApplicationExceptionIntegrationTests.groovy' (contained in this plugin) for example usage.

#####Automatic Refresh
Some domain models are modified within the database, which results in the persisted data being different than that in the domain model or Hibernate cache.  To simplify use of models where this situation exists, a '@DatabaseModifiesState' should be added to the model.  The 'ServiceBase' base class checks for the presence annotation after saving the model, and if present will perform a refresh before returning the model. Domain models that are backed by APIs (i.e., backed by views using instead of triggers that delegate to the API), or that are backed by tables that have triggers that modify the persistent state must have this annotation.  Services that do not extend or mixin ServiceBase should also check for this annotation and refresh the model when appropriate.

```groovy
@Entity
@Table( name="SOME_TABLE" )
@DatabaseModifiesState
class MyModel implements Serializable {
```
