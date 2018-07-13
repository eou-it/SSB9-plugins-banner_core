/*******************************************************************************
Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
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


