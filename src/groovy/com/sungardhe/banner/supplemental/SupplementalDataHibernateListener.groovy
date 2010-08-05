/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

import org.codehaus.groovy.grails.commons.ApplicationHolder

import org.hibernate.cfg.Configuration
import org.hibernate.event.Initializable
import org.hibernate.event.PreDeleteEvent
import org.hibernate.event.PreDeleteEventListener
import org.hibernate.event.PostLoadEventListener
import org.hibernate.event.PostLoadEvent
import org.springframework.context.ApplicationContext
import org.hibernate.event.PostInsertEvent
import org.hibernate.event.PostUpdateEvent
import org.hibernate.event.PostInsertEventListener
import org.hibernate.event.PostUpdateEventListener


/**
 * A listener that handles reading and writing of supplemental data properties into a model instance.
 */
public class SupplementalDataHibernateListener implements PreDeleteEventListener, PostInsertEventListener,
                                                          PostUpdateEventListener, PostLoadEventListener, Initializable {

    static detailed = false
    def supplementalDataService


    public void initialize( final Configuration config ) { }


    def getSupplementalDataService() {
        if (!supplementalDataService) {
            // fyi - it's ok if another thread also sneaks in...
            ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
            supplementalDataService = (SupplementalDataService) ctx.getBean( "supplementalDataService" )
        }
        supplementalDataService
    }


    // only returns entities that are configured to support supplemental data
    boolean supportsSupplementalData( event ) {
        if (event && event.getEntity()) {
            def entity = event.getEntity()
            getSupplementalDataService().supportsSupplementalProperties( entity.class )
        } else {
            false
        }
    }


    public boolean onPreDelete( final PreDeleteEvent event ) {
        try {
            if (supportsSupplementalData( event )) {
                handleDelete( event.getEntity() )
            }
        } catch (e) {
            e.printStackTrace()
            throw e
        }
        return false
    }


    public void onPostInsert( final PostInsertEvent event ) {
        try {
            if (supportsSupplementalData( event )) {
                handleInsertOrUpdate( event.getEntity() )
            }
        } catch (e) {
            e.printStackTrace()
            throw e
        }
    }


    public void onPostUpdate( final PostUpdateEvent event ) {
        try {
            if (supportsSupplementalData( event )) {
                handleInsertOrUpdate( event.getEntity() )
            }
        } catch (e) {
            e.printStackTrace()
            throw e
        }
    }


    public void onPostLoad( final PostLoadEvent event ) {
        try {
            if (supportsSupplementalData( event )) {
                handleLoad( event.getEntity() )
            }
        } catch (e) {
            e.printStackTrace()
            throw e
        }
    }


    def handleDelete( entity ) {
        if (entity.hasSupplementalProperties()) {
            getSupplementalDataService().removeSupplementalDataFor( entity )
        } 
    }


    def handleInsertOrUpdate( entity ) {
        if (entity.hasSupplementalProperties()) {
            getSupplementalDataService().persistSupplementalDataFor( entity )
        } 
    }


   def handleLoad( entity ) {
       getSupplementalDataService().loadSupplementalDataFor( entity )
   }

}
