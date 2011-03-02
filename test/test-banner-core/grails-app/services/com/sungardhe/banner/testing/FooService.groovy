/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import org.springframework.transaction.interceptor.TransactionAspectSupport
import com.sungardhe.banner.service.ServiceBase
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.interceptor.TransactionAttribute

// DEVELOPER NOTE:
// Basic CRUD methods (create, update, delete, list, count) methods are provided by the ServiceBase
// service.  Services may either extend ServiceBase or may mixin ServiceBzse (using the Mixin annotation).
// For backward compatibility with a previous 'method injection based appraoch', the mixin
// will also be mixed in if there is a 'static defaultCrudMethds = true' line.
//
// When 'mixing in' ServiceBase versus extending from it, @Transactional annotations are not effective.
// Consequently, if using @Mixin or including 'static defaultCrudMethods = false' (which will cause the ServiceBase
// to be mixed in dynamically during bootstrap), you must also include 'static transactional = true' to ensure
// your service is transactional.  If you extend ServiceBase, you should omit 'static transactional = true' as
// the @Transactional annotations in the ServiceBase are effective. @Transactional annotations provide more
// control as each method may be annotated, and each annotation may specify it's own transaction attributes.
//
// The ServiceBase CRUD methods may throw a runtime ApplicationException that should
// be caught (e.g., by a controller using this service). In addition to handling
// ApplicationException, controllers/composers should also catch Exception to ensure a desirable
// response is still provided to the user. Exceptions other than ApplicationException 
// are likely programming errors (as they were not wrapped in an ApplicationException).

// Please review ApplicationException and it's integration test for details on handling
// exceptions. The FooController in this project illustrates use of ApplicationException 
// functionality.   

/**
 * A transactional service supporting persistence of the Foo model.
 **/
// @Mixin( ServiceBase ) // Commented out, as we'll extend from ServiceBase in order to use @Transactional methods
class FooService extends ServiceBase {

    // There are THREE ways to mixin ServiceBase -- either by extending it (the preferred approach), mixing it
    // in using the @Mixin annotation or mixing it in dynamically by including the following
    // 'static defaultCrudMethods = true' line.  (As discussed above, mixing in ServiceBase precludes use of
    // @Transactianal annotations, and is thus not preferred.)
    //
    // static defaultCrudMethods = true

    // The following line would be required if we were mixing in ServiceBase. In this case we are extending from it
    // and thus it's @Transactional annotations are effective (precluding the need for using this static boolean)
    //
    //  

    // Note: You MUST set the domainClass IF you are not following normal naming conventions where the
    // domain name is the same as the service name (minus the trailing 'Service' portion).  Since in this case,
    // our domain name is 'Foo' and our service is 'FooService', the domainClass can be determined.
    //
    // Class domainClass = Foo 

    // When creating your own access/query methods, if you are extending ServiceBase you should specify a 'read-only'
    // transaction, as in the following annotation. This will be slightly more performant than using the static transactional = true
    // approach (where all methods would not be read-only, increasing Hibernate overhead.
    //
    // Note that for a simple service, with no additional methods like the test ones below, the body of the class would be completely empty.
    //
    
    
    // Example of a callback... this one gets called after the preValidationForUpdate callback and subsequent model validation
//    void preUpdate(map) { 
//        println map 
//    }


      // The models specified here will be 'refreshed' immediately after being created or updated. This is useful when 
      // the model is modified within the database, so that GORM can be made to reflect the state of the database. 
    public static databaseMayAlterPropertiesOf = [ Foo, Bar ]


    // ----------------------------------------- Test Methods ------------------------------------------

    
    @Transactional( readOnly = true, propagation = Propagation.REQUIRED )
    public boolean useReadOnlyRequiredTransaction() {
        def transAttributes = TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()
        TransactionAttribute.PROPAGATION_REQUIRED == transAttributes.propagationBehavior && transAttributes.readOnly
    }

    
    @Transactional( propagation = Propagation.REQUIRED )
    public boolean useRequiredTransaction() {
        def transAttributes = TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()
        TransactionAttribute.PROPAGATION_REQUIRED == transAttributes.propagationBehavior && !transAttributes.readOnly
    }


    @Transactional( propagation = Propagation.SUPPORTS )
    public boolean useSupportsTransaction() {
        def transAttributes = TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()
        TransactionAttribute.PROPAGATION_SUPPORTS == transAttributes.propagationBehavior && !transAttributes.readOnly
    }


    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public boolean useRequiresNewTransaction() {
        def transAttributes = TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()
        TransactionAttribute.PROPAGATION_REQUIRES_NEW == transAttributes.propagationBehavior && !transAttributes.readOnly
    }

}
