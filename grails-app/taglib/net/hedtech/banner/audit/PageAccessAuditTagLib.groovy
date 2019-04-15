/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.audit

class PageAccessAuditTagLib {
    def springSecurityService
    def pageAccessAuditService

    def pageAccessAudit = {
        if(springSecurityService.isLoggedIn()){
           // def pageAccessAudit = pageAccessAuditService.createPageAudit()
            println("******pageAccessAudit********"+pageAccessAudit)
        }
    }
}
