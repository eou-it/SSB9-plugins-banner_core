/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.exceptions

import grails.util.GrailsNameUtils

import org.springframework.validation.AbstractErrors
import org.springframework.validation.BindingResult
import org.springframework.validation.Errors


/**
 * A runtime exception used to hold validation errors for multiple models. 
 * The errors are held within a list, where each entry is a simple map as follows:
 * [ entityName: classSimpleName, id: id_or_null, errors: all_errors_for_this_model_instance ].
 **/
 class MultiModelValidationException extends RuntimeException {
    
    List modelValidationErrorsMaps // List items are maps: [ entityName: className, id: id, errors: listOfErrors ]

    
    // Factory method used to validate models and instantiate a MultiModelValidationException when necessary.    
    /**
     * Returns a MultiModelValidationException or null if no validation errors exist.
     * If objects are supplied which do not support 'validate' per grails model conventions, no errors 
     * will be reported for those objects. 
     **/
    public static MultiModelValidationException validate( List modelsToValidate ) {
        def modelErrorsMaps = [] // a list of maps, where each map includes the simple class name, the id, and the 'Errors' object for a specific model
        modelsToValidate?.each {
            if (it.respondsTo( "validate" ) && !(it.validate())) {
                modelErrorsMaps << [ entitySimpleClassName: it.class.simpleName, id: (it.hasProperty( 'id' ) ? it.id : null), errors: it.errors ]
            }
        }
        return ((modelErrorsMaps.size() > 0) ? new MultiModelValidationException( modelErrorsMaps ) : null)
    } 

        
    /**
     * Private constructor to prevent instantiation outside of this class' static factory method.
     **/
    private MultiModelValidationException( modelErrorsMaps ) {
        modelValidationErrorsMaps = modelErrorsMaps
    }

        
    /**
     * Constructor for a MultiModelValidationException, that may then be used to accumulate Errors.
     * An errors instance may optionally be provided, and errors objects may be added to this 
     * exception. 
     * @see #addErrors( Errors errors )
     * @param errors optionally provide an Errors instance containing errors for a model instance
     **/
    public MultiModelValidationException( Errors errors = null ) {
        modelValidationErrorsMaps = []
    }
    
    
    /**
     * Adds the supplied Errors object to this exception.  If an Errors object already exists for 
     * the specific model, then an IllegalStateException will be thrown.  That is, only one Errors object
     * corresponding to a model instance may be added. 
     * @param errors An Errors instance containing errors
     * @throws IllegalStateException if attempting to add a second Errors object for a model instance
     **/
    public addErrors( Errors errors ) {
        assert modelValidationErrorsMaps != null // All constructor's are expected to instantiate this map
        def entitySimpleClassName = GrailsNameUtils.getShortName( errors.objectName )
        def target = extractTarget( errors )
        def id = extractId( target )
        
        def mapForModel = modelValidationErrorsMaps.find { it.entitySimpleClassName == entitySimpleClassName && it.id == id }
        if (mapForModel) {
            // we'll throw an exception -- this is a programmer exception and should be caught via testing (hence it is not localized)
            throw new IllegalStateException( "Attempt to add an Errors object for a model instance, when an Errors object already exists!")
        } else {
            // we don't have an existing Errors for this model instance
            modelValidationErrorsMaps << [ entitySimpleClassName: entitySimpleClassName, id: id, errors: errors ]           
        }
    }
    
    
    // defaulted to a string that may be parsed to support localization.
    public String getMessage() {
        "@@r1:multi.model.validation.errors@@"
    }
    
    
    // Akin to the 'errors' property on a 'normal' ValidationException or model
    /**
     * Returns an Errors instance containing all errors (across all models that have validation errors).
     **/
    public Errors getErrors() {
        Errors accumulatedErrors = new MultiModelErrors()
        modelValidationErrorsMaps.each { 
            accumulatedErrors.addAllErrors( it.errors )    
        } 
        accumulatedErrors      
    }
    
    
    /**
     * Returns an errors object containing errors for a specific entity type.
     **/
    public Errors getErrorsFor( String entitySimpleClassName ) {
        List errorsForModelClass = modelValidationErrorsMaps?.findAll { it.entitySimpleClassName == entitySimpleClassName }
        def accumulatedErrors = new MultiModelErrors()
        errorsForModelClass.each { 
            accumulatedErrors.addAllErrors( it.errors ) 
        } 
        accumulatedErrors      
    } 
    
    
    /**
     * Returns an errors object containing errors for a specific model instance.
     **/
    public Errors getErrorsFor( String entityName, id ) {
        def errorsForModelInstance = modelValidationErrorsMaps?.find { it.entitySimpleClassName == entityName && it.id == id }
        errorsForModelInstance?.errors
    } 
    
    
    private def extractTarget( Errors errors ) {
        (errors instanceof BindingResult) ? errors.target : null
    }
    
    
    private def extractId( Object target ) {
        ((target && target.hasProperty( 'id' )) ? target.id : null)
    }
    
}


/**
 * Spring 'Errors' implementations support the collection of errors for a single entity, 
 * and verify that errors are not added that correspond to another entity. 
 * Consequently, a 'mini' implementation (most methods simply throw 'Not Supported' 
 * runtime exceptions) is used to return an 'Errors' like object that contains
 * errors for multiple entities. Note that this 'Errors' implementation exposes
 * the 'allErrors' property (e.g., that is used when wrapping an exception within an 
 * ApplicationException). 
 **/
class MultiModelErrors extends AbstractErrors {
    
    List allErrors = new ArrayList()
    
    void addAllErrors( Errors errors ) {
        allErrors.addAll( errors.allErrors )
    }
    
    List getFieldErrors() {
       allErrors // TODO: collect and return only field errors 
    }
    
    List getGlobalErrors() {
        allErrors // TODO: collect and return only global errors 
    }
    
    String getObjectName() {
        "Multiple Models"    
    }
    
    Object getFieldValue( String s ) {
        throw new RuntimeException( "Method Not Supported" ) 
    }
    
    void reject( String errorCode ) {
        throw new RuntimeException( "Method Not Supported" ) 
    }
    
    void reject( String errorCode, String defaultMessage ) {
        throw new RuntimeException( "Method Not Supported" ) 
    }
    
    void reject( String errorCode, Object[] errorArgs, String defaultMessage ) {
        throw new RuntimeException( "Method Not Supported" ) 
    }
    
    void rejectValue( String field, String errorCode ) {
        throw new RuntimeException( "Method Not Supported" ) 
    }
    
    void rejectValue( String field, String errorCode, String defaultMessage ) {
        throw new RuntimeException( "Method Not Supported" ) 
    }
    
    void rejectValue( String field, String errorCode, Object[] errorArgs, String defaultMessage ) {
        throw new RuntimeException( "Method Not Supported" ) 
    }
    
}

