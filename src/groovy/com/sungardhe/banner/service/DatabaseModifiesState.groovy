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
