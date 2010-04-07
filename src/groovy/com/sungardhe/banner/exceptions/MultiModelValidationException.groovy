/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.exceptions

import org.springframework.validation.AbstractErrors
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
    
    
    // defaulted to a string that may be parsed to support localization.
    public String getMessage() {
        "@@r1:default.multi.model.validation.errors@@"
    }
    
    
    // Akin to the 'errors' property on a 'normal' ValidationException or model
    /**
     * Returns an Errors instance containing all errors (across all models that have validation errors).
     **/
    public Errors getErrors() {
        Errors allErrors = new MultiModelErrors()
        modelValidationErrorsMaps.each { 
            allErrors.addAllErrors( it.errors )    
        } 
        allErrors      
    }
    
    
    /**
     * Returns an errors object containing errors for a specific entity type.
     **/
    public Errors getErrorsFor( String entitySimpleClassName ) {
        List errorMapForModel = modelValidationErrorsMaps?.findAll { it.entitySimpleClassName == entitySimpleClassName }
        Errors allErrors
        errorMapForModel.each { 
            if (!allErrors) {
                allErrors = it.errors
            } else {
                allErrors.addAllErrors(  it.errors )    
            }
        } 
        allErrors      
    } 
    
    
    /**
     * Returns an errors object containing errors for a specific model instance.
     **/
    public Errors getErrorsFor( String entityName, id ) {
        def modelErrorsMap = modelValidationErrorsMaps?.find { it.entitySimpleClassName == entityName && it.id == id }
        modelErrorsMap?.errors
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

