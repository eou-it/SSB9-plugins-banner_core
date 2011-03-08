/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.service

import java.lang.annotation.*


/**
 * A class-level annotation that may be used to indicate that the database may 
 * modify the state of the annotated model after a save. This occurs with models
 * that are backed by APIs (including those backed by views that have instead of 
 * triggers that delegate to APIs). Banner APIs set the 'activity date' and ignore the 
 * lastModified property value set via the 'AuditTrailPropertySupportHibernateListener'
 * hibernate listener.  This annotation is used by 'ServiceBase', so that services 
 * that extend or mixin ServiceBase will automatically refresh the model after a save.
 * If this refresh is not performed, the model will not reflect what is actually persisted.
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface DatabaseModifiesState { } // no annotation attributes are needed
