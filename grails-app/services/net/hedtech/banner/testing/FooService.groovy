/* ****************************************************************************
Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.testing

import net.hedtech.banner.exceptions.MepCodeNotFoundException
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.interceptor.TransactionAttribute
import org.springframework.web.context.request.RequestContextHolder

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
    // Note for a simple service, with no additional methods like the test ones below, the body of the class would be completely empty.
    //
    // We'll override 'read' so that we can support MEP testing.
    // Please see FooResourceApiFunctionalSpec.
    // Specifically, we throw an exception for a 'bad' MEP code and include a 'good'
    // mepCode as a supplemental rest property.
    //
    public def get( id ) {

        def session = RequestContextHolder.currentRequestAttributes()?.request?.session
        def mepCode = session?.getAttribute("mep")

        switch (mepCode) {
            case "MEPCODENOTFOUND_AE":
                def outer = new RuntimeException(new MepCodeNotFoundException(mepCode: "MEPCODENOTFOUND_AE"))
                def notFound = extractNestedNotFoundException(outer)
                throw new ApplicationException( getDomainClass(), notFound )
                break
            case "MEPCODENOTFOUND_RE":
                throw new RuntimeException(new MepCodeNotFoundException(mepCode: "MEPCODENOTFOUND_RE"))
                break
            case "MEPCODENOTFOUND":
                throw new MepCodeNotFoundException(mepCode: "MEPCODENOTFOUND")
                break
        }

        def foo = super.get( id )

        // We'll set the 'good' mepCode as supplementalRestData, so we can add
        // it to a representation as an 'affordance'.
        if (mepCode) {
            foo.metaClass.getSupplementalRestProperties << { -> [ 'mepCode': mepCode ] }
        }
        foo
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
