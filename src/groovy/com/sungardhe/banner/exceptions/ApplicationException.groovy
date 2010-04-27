/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.exceptions

import com.sungardhe.banner.exceptions.NotFoundException

import java.sql.SQLException

import javax.persistence.EntityExistsException

import grails.util.GrailsNameUtils
import grails.validation.ValidationException

import org.apache.log4j.Logger

import org.hibernate.StaleObjectStateException

import org.springframework.dao.DataIntegrityViolationException as ConstraintException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException as OptimisticLockException


// TODO: This implementation borrows code from Enrollment Management and needs to be refactored to be 'more groovy' 
/**
 * A runtime exception thrown from services (and other artifacts as necessary).  
 * This exception often wraps underlying runtime exceptions thrown by various libraries 
 * like Spring, Hibernate, Oracle, etc., and is used to provide a common interface for 
 * interogating the exception. 
 * Specific exceptions wrapped by this exception include:
 * update and delete may throw com.sungardhe.banner.exceptions.NotFoundException if the entity cannot be found in the database
 * update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
 * create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure      
 **/
class ApplicationException extends RuntimeException {    
    
    def    wrappedException    // a checked or runtime exception being wrapped
    def    sqlException        // set if the wrappedException is either a SQLException or wraps a SQLException
    String friendlyName        // a friendly name for the exception - exposed as 'type' property               
    String resourceCode = "default.internal.error" // usually set based upon a specific wrappedException, but defaulted as well
    String entityClassName    // the fully qualified class name for the associated domain model
    def    id                 // optional, the id of the model if applicable
    
    def log = Logger.getLogger( ApplicationException.name )


    /**
     * Constructor for an Application Exception.
     * @param entityClassOrName a Class or name of an entity that will be used when localizing messages 
     * @param e the Throwable to wrap within this application exception
     **/  
    public ApplicationException( entityClassOrName, Throwable e ) { 
        if (!entityClassOrName) entityClassOrName = ''  
        switch (entityClassOrName.class) {
            case (Class)  : this.entityClassName = entityClassOrName.name; break;
            case (String) : this.entityClassName = entityClassOrName; break;
            default       : this.entityClassName = entityClassOrName.toString(); 
                            log.warn "ApplicationException was given an entityClassOrName of type ${entityClassOrName.class}"
        }    
        wrapException( e )
    }
    
    
    /**
     * Constructor for an Application Exception.
     * @param entityClassOrName a Class or name of an entity that will be used when localizing messages 
     * @param msg the message that will be used to create a RuntimeException that will be wrapped
     **/  
    public ApplicationException( entityClassOrName, String msg ) {        
        this( entityClassOrName, new RuntimeException( msg ) )
    }
    
    
    /**
     * Constructor for an Application Exception.
     * @param entityClassOrName a Class or name of an entity that will be used when localizing messages 
     * @param id the id of the entity, if applicable
     * @param e the Throwable to wrap within this application exception
     **/  
    public ApplicationException( entityClassOrName, id, Throwable e ) {
        this( entityClassOrName, e )
        this.id = id
    }
    
    
    private def wrapException( e ) {
        
    }
        
    
    // This is the primary interface 'method' (really a closure) used by controllers.
    // The 'returnMap' map provides a common structure, that may be rendered via a gsp page,  
    // or converted to json or xml using Grails Converters.  Controllers must supply
    // a 'localizer' closure that wraps the message tag library that Grails automatically injects 
    // into controllers (which is accessible as a method). This closure is used 
    // to lookup localized messages specified in the i18n property files). 
    //
    // The 'returnMap' is in the following form:
    //     [ message: 'The localized message that should be presented to the user',
    //       errors: [ 'localized error 1', 'localized error 2' ] (when available; often 'errors' will be 'null')
    //       success: true|false 
    //       underlyingErrorMessage: 'The message from the underlying exception' (in general, not appropriate to show to the user)
    //     ]
    //
    // The 'message' and the optional list of 'errors' are both localized and intended for display. 
    //
    public def returnMap = { message ->
        // invoke closure with name being the LCC name for the underlying exception class
        // in order to return the map that is returned by the invoked closure
        def mapToReturn
        if (exceptionHandlers[ getType() ]) {
            mapToReturn = exceptionHandlers[ getType() ]( message )
        } else {
            mapToReturn = exceptionHandlers[ 'AnyOtherException' ]( message )
        }
        (mapToReturn + [ success: false, underlyingErrorMessage: wrappedException.message ])
    }
            
    
    public String getType() {        
        def name = wrappedException.class.simpleName
        if (name == 'HibernateOptimisticLockingFailureException') {
            name = 'OptimisticLockException'
        } else if (name == 'DataIntegrityViolationException') {
            if (wrappedException.getRootCause() instanceof EntityExistsException) {
                name = 'EntityExistsException'
            } else if (extractSQLException( wrappedException )) {
                name = 'SQLException'
            } else {
                name = 'ConstraintException'
            }
        } else if (name == 'UncategorizedDataAccessException') {
            name = 'UnknownException'
        }
        name
    }   
    
    
    private String getUserFriendlyName() {
        GrailsNameUtils.getNaturalName( GrailsNameUtils.getShortName( entityClassName ) )
    }
    
    
    // The following map associates closures with wrapped exceptions, in order to return  
    // specific, localized 'returnMap' values (i.e., message, args, errors) versus only 
    // generic values.  Controllers may return this 'returnMap', or convert it to JSON or XML
    // using the converters before returning it. 
    // That is, by adding a closure for an exception that may be encountered, this 
    // exception class can use specifc values when returning the standard 'returnMap'  
    // map that provides a common interface that may be used by controllers. 
    //
    private def exceptionHandlers = [  
    
        // An exception we throw explicitly within our services when GORM is not able to find an entity
        'NotFoundException': { localize -> 
            [ message: localize( code: 'default.not.found.message', 
                                 args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
              errors: null
            ]            
        },

        // May be thrown when a record cannot be found in the database, when using SQL or when using GORM with failOnError. 
        // This exception is less desirable, as it does not carry the 'id' of the entity that could not be found.
        'DataRetrievalFailureException': { localize ->  
            [ message: localize( code: 'default.not.found.message', 
                                 args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ), '' ] ) as String,
              errors: null
            ]            
        },
        
        'OptimisticLockException': { localize ->
            [ message: localize( code: 'default.optimistic.locking.failure', 
                                 args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
              errors: null
            ]            
        },
        
        'ValidationException': { localize ->
            [ message: localize( code: 'default.validation.errors.message',
                                 args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
              errors: wrappedException.errors?.allErrors?.collect { localize( error: it ) }
            ]
         },
        
        'MultiModelValidationException': { localize ->
            String msg
            if (wrappedException.message?.startsWith("@@r1")) {
                def rcp = getResourceCodeAndParams( wrappedException.message )
                msg = localize( code: rcp.resourceCode, args: rcp.bindingParams ) as String
            } else {
                msg = wrappedException.message
            }
            [ message: msg,
              errors: wrappedException.errors?.allErrors?.collect { localize( error: it ) },
              modelValidationErrorsMaps: wrappedException.modelValidationErrorsMaps
            ]
         },
        
        'ConstraintException': { localize ->
            // A SQLException may be wrapped, a number of times, by one or more hibernate and Spring exceptions
            def sqlException = extractSQLException( wrappedException )
            if (sqlException) {
                // We can't just return a map, since many exceptions fall into this category and require specialized processing
                createReturnMapForSQLException( sqlException, localize )
            } else {
                [ message: localize( code: 'default.constraint.error.message', 
                                     args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
                  errors: null
                ] 
            }
         },
        
        // This includes Banner API Exceptions. 
        'SQLException': { localize ->
            // Although the 'type' is SQLException, it may 'still' be wrapped inside another exception...
            def sqlException = (wrappedException instanceof SQLException) ? wrappedException : extractSQLException( wrappedException )
            
            // We can't just return a map, since many exceptions fall into this category and require specialized processing
            createReturnMapForSQLException( sqlException, localize )
        },
        
        'EntityExistsException': { localize ->
            [ message: localize( code: 'default.entity.exists.error.message', 
                                 args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
              errors: null
            ]
         },
        
        'AnyOtherException': { localize ->
            if (wrappedException.message.startsWith( "@@r1")) {
                def rcp = getResourceCodeAndParams( wrappedException.message )                               
                return [ message: localize( code: rcp.resourceCode, args: rcp.bindingParams ) as String,
                         errors:  (wrappedException.hasProperty( 'errors' ) ? wrappedException.errors?.allErrors?.collect { message( error: it ) } : null) 
                       ]
            } else {
                log.error "ApplicationException cannot localize or handle it's wrapped exception $wrappedException"
                [ message: "Sorry, an unexpected error has occurred: ${wrappedException.message}", // If this is an unmapped message, we won't localize at all...
                  errors:  (wrappedException.hasProperty( 'errors' ) ? wrappedException.errors?.allErrors?.collect { message( error: it ) } : null)
                ]
            }            
        }
    ]
            
    
    public String toString() {
        def response = "ApplicationException:[type=${this.getType()}, entityClassName=$entityClassName"
        if (id) { 
            response = response + ", id= $id"
        }
        def errors = (wrappedException.hasProperty( 'errors' ) ? wrappedException.errors?.allErrors?.each { error: it } : null)
        if (errors) {
            response = response + ", errors='${errors.join( " ** ")}'"
        }
         
        def sqlException = (wrappedException instanceof SQLException) ? wrappedException : extractSQLException( wrappedException )

        if (sqlException != null) {
            response = response + ", wrappedSQLException(errorCode=${sqlException.errorCode}, message=${sqlException.message})"
        } else {
            response = response + ", wrappedException(message=${wrappedException?.message})"
        }
        response = response + "]"
        response
    } 
    
    
    // ------------------------------------ Private Methods -----------------------------------------
    
    
    // We have a constraint exception that requires additional processing to extract desired error messages.
    // Banner API Exceptions are handled here. 
    private def createReturnMapForSQLException( sqlException, localize ) {
        def type // used when creating an appropriate integrity constraint resourceCode
        
        switch (sqlException.getErrorCode()) {
            case 1 : // 'Unique Exception' 
                log.error "A 'Unique' constraint exception was encountered that apparently was not \
                          caught as a validation constraint, for $entityClassName with id=$id", sqlException
                return [ message: localize( code: 'default.not.unique.message', 
                                            args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
                         errors: null 
                       ]
            case 2290 : // 'Check' constraint -- This is considered a programming error, as this can be avoided using model validation constraints
                log.error "A 'Check' constraint exception was encountered which may be better handled \
                          by adding a validation constraint to the $entityClassName with id $id", sqlException
                return [ message: localize( code: 'default.constraint.error.message', 
                                            args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
                         errors: null 
                       ]
            case 2291 :  
                type = 'parentNotFound'  // handled below...                            
            case 2292 : 
                type = type ? type : 'childExists'
                
                // This isn't necessarily an error -- some constraint exceptions will likely be expected... we'll log as an error (at least initially)
                // so the logging has access to the exception...
                log.error "An 'integrity.$type' constraint exception was encountered which may be better handled \
                           by adding a validation constraint to the $entityClassName with id $id", sqlException

                String constraintName = getConstraintName( sqlException.message )                
                def resourceCode = (constraintName) ? "$entityClassName.$type.$constraintName" : 'default.constraint.error.message' 
                               
                return [ message: localize( code: resourceCode, 
                                            args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
                         errors: null 
                       ]
                       
            // The following codes indicate a Banner API exception that contains a resource code that should be mapped within our property files. 
            // In this case, we return a returnMap with a localized message (using the resource code and binding parameters extracted from the exception)
            // and leave the errors list null (as this type of Banner API Exception is used to report only a single error).            
            case -20200 : // handled below        
            case  20200 : // handled below       
            case -20101 : // handled below
            case  20101 : // Banner API exceptions that tunnel resource codes  
                def rcp = getResourceCodeAndParams( sqlException )                               
                return [ message: localize( code: rcp.resourceCode, 
                                            args: rcp.bindingParams ) as String,
                         errors: null 
                       ]
                       
            // The following codes indicate a Banner API exception that may contain multiple 'pre-localized' error messages within it.
            // In this case, we return the returnMap with a default localized message and return the pre-localized message(s) as the errors list.  
            case -20100 : // handled below
            case  20100 : // handled below
            case -28115 : // handled below
            case  28115 : // Banner API exceptions that contain full messages (localized per Banner)
                return [ message: localize( code: 'default.banner.api.error', 
                                            args: [ localize( code: "${entityClassName}.label", default: getUserFriendlyName() ) ] ) as String,
                         errors: extractErrorMessages( sqlException.getMessage() )
                       ]
                       
            default :
                return [ message: localize( code: 'default.banner.api.error', 
                                            args: null ) as String,
                         errors: sqlException.getMessage() // may be ugly...
                       ] 
        }
        
    } 
        
    
    // Extracts and returns a SQLException if present, else returns null. 
    private SQLException extractSQLException( Throwable e ) {
        def SQLException sqlException
        while (e != null && !(e instanceof SQLException)) {
            e = e.getCause()
        }
        if (e instanceof SQLException) {
            (SQLException) e
        } else {
            null
        }
    }
    
    
    // This method lifted, largely intact, from the 'ExceptionMapper' class within the 
    // Enrollment Management project. TODO: Make more groovy. 
    /**
     * Returns a constraint name if one can be parsed from the supplied exception message.
     * @param message the exception message that may contain a constraint name
     * @return String the constraint name or null if one couldn't be identified
     */
    private String getConstraintName( String message ) {
        log.debug( "ExceptionMapper given exception message: " + message );
        
        int templateStartPosition = message.indexOf( "constraint (" )
        if (templateStartPosition < 0) {
            return null
        }

        int start = templateStartPosition + "constraint (".length()
        int end = message.indexOf( ") violated", start )
        if ( end < 0 ) {
            end = message.length()
        }

        String constraintName = stripSchemaNameFrom( message.substring( start, end ) )
        log.debug( "ExceptionMapper will return substring from " + start + " to " + end )
        constraintName
    }
    
    
    // This method lifted, largely intact, from the 'ExceptionMapper' class within the 
    // Enrollment Management project. TODO: Make more groovy. 
    /**
     * Returns a constraint name by stripping the schema name from the supplied constraintName.
     * @param constraintName the constraint name that includes the schema name
     * @return String a constraint name without the schema name
     */
    private String stripSchemaNameFrom( String constraintName ) {
        int templateStartPosition = constraintName.indexOf( "." );
        if (templateStartPosition < 0) {
            return constraintName;
        }
        int start = templateStartPosition + ".".length();
        return constraintName.substring( start, constraintName.length() );
    }
    
    
    // Used for Banner API Exception's that are not 'pre-localized' but instead embed a resourceCode and 
    // parameters within the exception's message.
    private Map getResourceCodeAndParams( SQLException sqlException ) { 
        getResourceCodeAndParams( sqlException?.getMessage() )
    }    
        
    
    // The following methods are used for handling Banner API exceptions. Much of this implementation is 
    // taken directly from the Enrollment Management project.  As issues arise, you will probably want to 
    // review the EM code to see if changes made their address the issue. 
    
    // Used for Banner API Exception's that are not 'pre-localized' but instead embed a resourceCode and 
    // parameters within the exception's message.
    private Map getResourceCodeAndParams( String msg ) {
        List<String> bindingParams = parse( extractAPIErrorText( msg ) )
        if (bindingParams.size() < 2) {
            log.error "Exception message did not contain parsable content: $msg"
            resourceCode = "default.unknown.banner.api.exception"
        }
        if (!bindingParams.get( 0 ).equals( "r1" )) {
            log.error "Unknown tunneled exception; message should have started with 'r1' but was: $msg"
            resourceCode = "default.unknown.banner.api.exception"
        }
        String resourceCode = bindingParams.get( 1 )
        bindingParams.remove( 0 )
        bindingParams.remove( 0 )
        
        [ resourceCode: resourceCode, bindingParams: bindingParams ]
    }
    
    
    // Used for Banner API Exception's that are 'pre-localized' within the database.
    // Lifted from the BannerApiException class within the Enrollment Management project.
    /**
     * Returns a list of error messages that were extracted from an exception
     * thrown by a Banner API.  Exceptions thrown from Banner APIs are always in
     * the following form:
     * ::this is the first error::::this is the second error::::this is the third one::
     * @param exceptionMessage the exception message that may contain multiple error messages
     * @return List<String> the list of error messages, guaranteed to contain at least one message
     */
    private List<String> extractErrorMessages( String exceptionMessage ) {
        if (exceptionMessage == null || exceptionMessage.length() == 0) {
            return new ArrayList<String>()
        }

        String[] errorMessages = exceptionMessage.split( "[:]{2,4}" )
        List<String> result = new ArrayList<String>()

        if (errorMessages != null && errorMessages.length > 0) {
            // make sure we don't include any empty strings, the ORA- code, or an
            // Oracle SQLException stack trace.
            for (String msg : errorMessages) {
                if (isEndUserErrorMessage( msg )) {
                    result.add( msg )
                }
            }
        }
        result
    }


    // Used for Banner API Exception's that are 'pre-localized' within the database.
    // Lifted from the BannerApiException class within the Enrollment Management project.
    /**
     * Returns true if the supplied error message contains an ORA- error code or
     * a low-level stack trace.  This is used to identify these error strings from
     * the end-user appropriate strings.
     * @param errorMessage the error string that may be end user appropriate or may contain technical details
     * @return true if this error message is end-user appropriate
     */
    private boolean isEndUserErrorMessage( String errorMessage ) {
        return errorMessage != null && errorMessage.length() > 0 && !(errorMessage.startsWith( "ORA-" ) || errorMessage.startsWith( "ORA-", 1 ))
    }
    
    
    // Used for Banner API Exception's that are not 'pre-localized' but instead have a resourceCode and params.
    // This method lifted, fairly intact, from the LocalizableSqlApiExceptionExtractor class within the 
    // Enrollment Management project.
    // TODO: Re-implement to be more 'groovy'
    /**
     * Yank out the error message text from exceptions from api calls Message is wrapped with @@ Ex:
     * "ORA-20200 :
     * @@r1:resource code:param1:param2@@\nORA-06512 at ...."
     */
    private String extractAPIErrorText( String message ) {
        if (message == null) {
            return message
        }
        String tmp = message.substring( message.indexOf( "@@" ) + 2, message.length() )
        int end = tmp.indexOf( "@@" )
        return tmp.substring( 0, end )
    }
    
    
    // Used for Banner API Exception's that are not 'pre-localized'.
    // This method lifted, fairly intact, from the LocalizableSqlApiExceptionExtractor class within the 
    // Enrollment Management project.  
    // TODO: Re-implement to be more 'groovy'
    /**
     * Parses the specified string and returns an array of values. The String is assumed to consist
     * of one or more parameters delimited by colons. Colons within a parameter can be escaped by a
     * backslash; two backslashes must be used to indicate a single backslash in a parameter.
     */
    private List<String> parse( String source ) {
        List<String> values = new ArrayList<String>()
        String currentChar = null
        StringBuffer sb = new StringBuffer()
        boolean lastCharWasEscape = false
        for (int i = 0; i < source.length(); i++) {
            currentChar = source.substring( i, i + 1 )
            if (lastCharWasEscape) {
                if (currentChar.equals( "\\" )) {
                    lastCharWasEscape = false
                    sb.append( "\\" )
                    currentChar = null
                } else if (currentChar.equals( ":" )) {
                    lastCharWasEscape = false
                    sb.append( ":" )
                    currentChar = null
                } else {
                    // didn't escape anything, discard
                    sb.append( currentChar )
                    lastCharWasEscape = false
                    currentChar = null
                }
            } else if (currentChar.equals( "\\" )) {
                lastCharWasEscape = true
            } else if (currentChar.equals( ":" )) {
                // field separater
                values.add( sb.toString() )
                sb = new StringBuffer()
                currentChar = null
            } else {
                sb.append( currentChar )
                currentChar = null
            }
        }
        if (currentChar != null) {
            sb.append( currentChar )
        }
        values.add( sb.toString() )
        return values
    }
        

}