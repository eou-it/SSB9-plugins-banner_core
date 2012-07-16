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
package net.hedtech.banner.security


/**
 * A thread local to hold the current 'form context'. A filter will set one or more
 * Banner names into this context based upon which controller is being accessed.  A mapping of
 * the Grails controller names to a list of 'related' Banner form names is used by the filter
 * to set the appropriate forms into this thread local.
 * This allows existing Banner security roles to be used more effectively.
 */
public class FormContext {


    private static ThreadLocal storage = new ThreadLocal()


    public static List<String> get() {
        storage.get() as List
    }


    public static void set( List formNames ) {
        storage.set formNames
    }


    public static void clear() {
        storage.set null
    }
    
    
    public static boolean isSelfService() {
        storage.get() && storage.get()[0].contains( "SELFSERVICE" )
    }


}
