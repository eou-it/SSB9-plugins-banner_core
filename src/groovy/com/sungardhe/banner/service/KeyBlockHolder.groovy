/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.service


// Note that keyblock may be passed explicitly, as part of a map, when invoking service methods. 
// This threadlocal-based approach is intended as a preferred alternative to the current approach. 
// It is recommended that composers set this threadlocal in lieu of including it within the map
// argument when invoking service methods. 

/**
 * This may be used to hold the keyblock for the current request, as a means to 
 * share this 'context' across the various artifacts participating in handling the request.
 * Keyblock is a UI concept, and is thus optional.  Other UIs, RESTful APIs, etc. may 
 * not provide a keyblock.  
 **/
class KeyBlockHolder {
    

    private static ThreadLocal storage = new ThreadLocal()


    public static Map get() {
        storage.get() as Map
    }


    public static void set( Map keyBlock ) {
        storage.set keyBlock
    }


    public static void clear() {
        storage.set null
    }   
    
}
