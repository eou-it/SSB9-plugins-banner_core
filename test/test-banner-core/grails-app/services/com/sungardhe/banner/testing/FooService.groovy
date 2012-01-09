/* ********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.testing

import com.sungardhe.banner.service.KeyBlockHolder
import com.sungardhe.banner.service.ServiceBase

import org.springframework.transaction.interceptor.TransactionAspectSupport
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
// Consequently, if using @Mixin or including 'static defaultCrudMethods = true' (which will cause ServiceBase
// to be mixed-in dynamically during bootstrap), you should also include 'static transactional = true' to ensure
// your service is transactional.
//
// If you extend ServiceBase, you should omit 'static transactional = true' as
// the @Transactional annotations in the ServiceBase are effective. @Transactional annotations provide more
// control as each method may be annotated, and each annotation may specify it's own transaction attributes.
//
// The ServiceBase CRUD methods may throw a runtime ApplicationException that should
// be caught (e.g., by a controller using this service). In addition to handling
// ApplicationException, controllers/composers should also catch Exception to ensure a desirable
// response is still provided to the user. Exceptions other than ApplicationException 
// are likely programming errors (as they were not wrapped in an ApplicationException).

// Please review ApplicationException and it's integration test for details on handling
// exceptions. The RestfulControllerMixin in this project illustrates use of ApplicationException 
// functionality.

/**
 * A transactional service supporting persistence of the Foo model.
 **/
@Transactional
class FooService extends ServiceBase {

    // There are THREE ways to mixin ServiceBase -- either by extending it (the preferred approach), mixing it
    // in using the @Mixin annotation or mixing it in dynamically by including the following
    // 'static defaultCrudMethods = true' line.  (As discussed above, mixing in ServiceBase precludes use of
    // @Transactianal annotations, and is thus not preferred.)
    //
    // The following line would be required if we were mixing in ServiceBase. In this case we are extending from it
    // and thus it's @Transactional annotations are effective (precluding the need for using this static boolean)
    //
    // static defaultCrudMethods = true

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

    // The 'testKeyBlock' field is used solely for testing, and simply exposes the keyBlock if one was provided to the service
    // either within a map or via the KeyBlockHolder threadlocal.
    // Foo (or College) doesn't need or use a key block - again, this is solely for testing the framework. 
    private def testKeyBlock
    public def getTestKeyBlock() { testKeyBlock }

    // This preUpdate callback is used solely for testing. It facilitates access to the keyBlock when it exists. 
    void preUpdate( inputArg ) {
        testKeyBlock = getKeyBlock( inputArg )
    }


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
