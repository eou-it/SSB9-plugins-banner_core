/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.configuration

/**
 * Common Supplemental Data Engine utilities.
 */
class SupplementalDataUtils {

    // Converts View name to Table name
    public static def getTableName(tableName) {
        if (tableName.indexOf("SV_") == 0 ||
            tableName.indexOf("GV_") == 0 ||
            tableName.indexOf("FV_") == 0)

            return tableName.substring(3)
            return tableName
        
    }
}
