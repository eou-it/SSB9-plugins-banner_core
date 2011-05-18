/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.security

class BannerAuthenticationEvent extends org.springframework.context.ApplicationEvent {

    def userName
    def isSuccess
    def message
    def date
    def module
    def severity

    BannerAuthenticationEvent( def userName, def isSuccess, def message, def module, def date, severity ) {
        super('BannerAuthenticationProvider')
        this.userName = userName
        this.isSuccess = isSuccess
        this.message = message
        this.date = date
        this.module = module
        this.severity = severity
    }
}
