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
