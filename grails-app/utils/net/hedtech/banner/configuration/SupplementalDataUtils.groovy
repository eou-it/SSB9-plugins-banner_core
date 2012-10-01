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
