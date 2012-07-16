/*********************************************************************************
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
package net.hedtech.banner.supplemental

import org.codehaus.groovy.grails.commons.ApplicationHolder

import org.hibernate.cfg.Configuration
import org.hibernate.event.Initializable
import org.hibernate.event.PostLoadEventListener
import org.hibernate.event.PostLoadEvent
import org.springframework.context.ApplicationContext

/**
 * A listener that handles loading of supplemental data properties into a just-loaded model instance.
 *
 * Note: This listener is not used to persist supplemental data, as the listener would not be
 * notified when attempting to persist model instances that are not dirty (i.e., that have no changes
 * to their 'normal' properties) but that have changes only to their supplemental data.  Consequently,
 * persistence is performed explicitly when saving a model via a service that extends from, or mixes in,
 * ServiceBase.
 * @see net.hedtech.banner.service.ServiceBase
 */
public class SupplementalDataHibernateListener implements PostLoadEventListener, Initializable {

    static detailed = false
    SupplementalDataService supplementalDataService


    public void initialize( final Configuration config ) { }


    def getSupplementalDataService() {
        if (!supplementalDataService) {  // fyi - it's ok if another thread also sneaks in...
            ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
            supplementalDataService = (SupplementalDataService) ctx.getBean( "supplementalDataService" )
        }
        supplementalDataService
    }


    public void onPostLoad( final PostLoadEvent event ) {
        try {
            if (supportsSupplementalData( event )) {
                getSupplementalDataService().loadSupplementalDataFor( event.getEntity() )
            }
        } catch (e) {
            e.printStackTrace()
            throw e
        }
    }


    private boolean supportsSupplementalData( event ) {
        if (event && event.getEntity()) {
            return getSupplementalDataService().supportsSupplementalProperties( event.getEntity().class )
        }
        false
    }

}
