/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.configuration

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
