/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.audit

import net.hedtech.banner.general.audit.PageAccessAudit

class PageAccessAuditTagLib {
    def springSecurityService
    def pageAccessAuditService

    def pageAccessAudit = {
        try{
            PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
            log.debug("PageAccess Audit created successfully with id ${pageAccessAudit.id}")
        } catch (ex){
            log.error("Exception occured while executing pageAccessAudit " + ex.getMessage())
        }
    }
}
