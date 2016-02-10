/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import net.hedtech.banner.service.ServiceBase
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import groovy.sql.Sql

/**
 * A transactional service supporting persistence of the Zip model.
 **/
class ZipService extends ServiceBase {

    def sql

    @Transactional( readOnly = true, propagation = Propagation.REQUIRED )
    public ZipForTesting fetch( long id ) {
        ZipForTesting.get( id )
    }

    public testSimple() {
       // log.debug("testSimple() started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.execute("Select 1 from dual")
        sql.connection.close()
       // log.debug("testSimple() completed" )

    }
    
    
}
