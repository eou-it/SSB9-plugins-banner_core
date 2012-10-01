/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.configuration

import java.util.regex.Matcher
import java.util.regex.Pattern

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

    public static dbCaseToCamelCase (dbCase) {
        Pattern p = Pattern.compile("[a-zA-Z]+");
        Matcher m = p.matcher(dbCase);
        StringBuffer result = new StringBuffer();
        String word;
        while (m.find()) {
            word = m.group();
            result.append(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
        }
        String finerResult = result.toString()
        finerResult = finerResult.substring(0,1).toLowerCase() +finerResult.substring(1)
        return finerResult
    }

    public static formatProperty (String prop, String tableName) {
        StringBuffer sb = new StringBuffer(prop)
        sb = sb.delete(0, (tableName+"_").length())
        return dbCaseToCamelCase(sb.toString())
    }

}
