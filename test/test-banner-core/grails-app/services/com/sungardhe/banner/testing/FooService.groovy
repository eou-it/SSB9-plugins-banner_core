/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import org.springframework.transaction.annotation.*


// NOTE:
// This service is injected with create, update, and delete methods.
// These injected CRUD methods may throw a runtime ApplicationException that should
// be caught (e.g., by a controller using this service). In addition to handling
// ApplicationException, controllers should also catch Exception to ensure a desirable
// response is still provided to the user. Exceptions other than ApplicationException 
// are likely programming errors (as they were not wrapped in an ApplicationException).

// Please review ApplicationException and it's integration test for details on handling
// exceptions. The FooController in this project illustrates use of ApplicationException 
// functionality.   

// NOTE: Spring @Transactional currently not supported (it results in classpath exceptions
//       attempting to cast a Spring proxy to this service. Needs more investigation.)

/**
 * A transactional service supporting persistence of the College model. 
 **/
//@Transactional
class FooService {

    boolean transactional = true
    def transactionManager // injected by spring, used for testing purposes -- not normally needed in a service

    // Note: The defaultCrudMethods injected into this class will be transactional
    // based upon the class-level @Transactional annotation specified above. 
    static defaultCrudMethods = true
    
    
    // When creating your own access methods, you should specify a 'read-only' 
    // transaction, as follows. This will be slightly more performent than the 
    // accessors provided by the injected methods. (You may even want to override the 
    // the accessors that would be injected, so readOnly can be specified.)
//    @Transactional(readOnly = true)
    public Foo fetch( long id ) {
        Foo.get( id )
    }
    
    
}
