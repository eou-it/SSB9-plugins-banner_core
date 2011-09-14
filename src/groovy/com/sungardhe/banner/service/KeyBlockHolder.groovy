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


// Note that keyblock may be passed explicitly, as part of a map, when invoking service methods. 
// This threadlocal-based approach is intended as a preferred alternative to the current approach. 
// It is recommended that composers set this threadlocal in lieu of including it within the map
// argument when invoking service methods. 

/**
 * This may be used to hold the keyblock for the current request, as a means to 
 * share this 'context' across the various artifacts participating in handling the request.
 * Keyblock is a UI concept, and is thus optional.  Other UIs, RESTful APIs, etc. may 
 * not provide a keyblock.  The keyBlock is by default considered 'required' (mainly 
 * to avoid changes to composers), but may be marked as optional by calling 'markAsOptional'.
 **/
class KeyBlockHolder {
    

    private static ThreadLocal keyBlockStorage = new ThreadLocal()
    private static ThreadLocal optionalFlagStorage = new ThreadLocal() // keyblock is 'required' if this is empty

    
    /**
     * Returns the keyBlock held within this holder or supplied map.
     * If the keyBlock is not found in the holder, the supplied map will be checked.
     * If there is no keyBlock AND 'isOptional()' is true, a null will be returned.
     * If there is no keyBlock yet a keyBlock is required, a map with a single (dummy)
     * key is returned (so logic checking for keyBlock existence first can proceed and 
     * fail with appropriate exceptions). 
     * @param map an optional parameter, that may contain a keyBlock
     * @return Map a keyBlock map or either null or a dummy map if one was not found
     **/
    public static def get( Map map = null ) {
        def kb = keyBlockStorage.get() 
        if (!kb && map) kb = map.keyBlock
        // if a keyBlock is required and we don't have one, we'll return a map with 
        // something in it (so it can be used in booleans that conditionally validate 
        // keyBlocks when keyBlocks are required.)
        if (!kb && !isOptional()) kb = [ keyBlockPresence: 'No keyblock available, but one is required' ]
        kb
    }


    /**
     * Sets a map representing keyBlock information into this holder. 
     **/
    public static void set( keyBlock ) {
        keyBlockStorage.set keyBlock
    }


    /**
     * Clears the holder of a keyBlock and any indicator that it is optional. 
     **/
    public static void clear() {
        keyBlockStorage.set null
        optionalFlagStorage.set null
    }  
    
    
    /**
     * Establishes that a keyBlock is optional even for services that normally use a keyBlock. 
     * Default is that a keyBlock is required. Consequently, 'composers' and 'integration tests' 
     * will not normally use this method, however RESTful controllers will generally use this 
     * method (except for any cases where keyBlock is included in the resource representation). 
     *
     * If this is invoked on this holder, subsequent validation that uses a keyBlock will 
     * not be executed (and thus will not fail) if a keyBlock is not available. 
     * When 'clear' is invoked on this holder, the default that a keyBlock is required 
     * is re-established.  
     **/
    public static void markAsOptional() {
        optionalFlagStorage.set true    
    }
    
    
    /**
     * Returns true if a keyBlock is considered optional.
     * Note a keyBlock is considered optional if 'markAsOptional()' is called on this holder.
     * Also note that 'clear' will clear this indicator (making keyBlock required again).
     **/
    public static boolean isOptional() {
        optionalFlagStorage.get() ? true : false
    }
    
}
