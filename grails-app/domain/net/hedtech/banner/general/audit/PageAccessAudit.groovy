/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import javax.persistence.*

@Entity
@Table(name = 'GURASSA')
@NamedQueries(value = [
        @NamedQuery(name = "PageAccessAudit.fetchByLoginId",
                query = """ FROM PageAccessAudit pageAccessAudit WHERE pageAccessAudit.loginId = :loginId """),

        @NamedQuery(name = 'PageAccessAudit.fetchByAppId',
                query = """ FROM PageAccessAudit pageAccessAudit WHERE pageAccessAudit.appId = :appId """),
])
class PageAccessAudit implements Serializable{
    @Id
    @Column(name = 'GURASSA_SURROGATE_ID')
    @SequenceGenerator(name = "GURASSA_SEQ_GEN", sequenceName = "GURASSA_SURROGATE_ID_SEQUENCE",allocationSize=1)
    @GeneratedValue(generator = "GURASSA_SEQ_GEN", strategy = GenerationType.SEQUENCE)
    Long id

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GURASSA_AUDIT_TIME')
    Date auditTime

    @Column(name = 'GURASSA_SSB_LOGIN_ID')
    String loginId

    @Column(name = 'GURASSA_PIDM')
    Integer pidm

    @Column(name = 'GURASSA_APP_ID')
    String appId

    @Column(name = 'GURASSA_PAGE_URL')
    String pageUrl

    @Column(name = 'GURASSA_IPADDR')
    String ipAddress

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GURASSA_ACTIVITY_DATE')
    Date lastModified

    @Column(name = 'GURASSA_USER_ID')
    String lastModifiedBy

    @Column(name = 'GURASSA_DATA_ORIGIN')
    String dataOrigin

    @Column(name = 'GURASSA_VERSION')
    Long version

    @Column(name = 'GURASSA_VPDI_CODE')
    String vpdiCode


    static constraints = {
        auditTime(nullable:false)
        loginId(nullable:false, maxSize: 256)
        pageUrl(nullable:false, maxSize: 2000)
        appId(nullable:false, maxSize:10)
        ipAddress(nullable:false, maxSize: 50)
        pidm(nullable:true)
        dataOrigin(nullable: true, maxSize: 30)
        lastModified(nullable:true)
        lastModifiedBy(nullable:true, maxSize: 30)
        version(nullable:true)
        vpdiCode(nullable:true, maxSize: 6)
    }

    int hashCode() {
        int result
        result = (auditTime != null ? auditTime.hashCode() : 0)
        result = 31 * result + (loginId != null ? loginId.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (appId != null ? appId.hashCode() : 0)
        result = 31 * result + (pageUrl != null ? pageUrl.hashCode() : 0)
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (vpdiCode != null ? vpdiCode.hashCode() : 0)
        return result
    }

    boolean equals(o) {

        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PageAccessAudit pageAccessAudit = (PageAccessAudit) o

        if (auditTime != pageAccessAudit.auditTime) return false
        if (loginId != pageAccessAudit.loginId) return false
        if (pidm != pageAccessAudit.pidm) return false
        if (appId != pageAccessAudit.appId) return false
        if (pageUrl != pageAccessAudit.pageUrl) return false
        if (ipAddress != pageAccessAudit.ipAddress) return false
        if (lastModified != pageAccessAudit.lastModified) return false
        if (lastModifiedBy != pageAccessAudit.lastModifiedBy) return false
        if (dataOrigin != pageAccessAudit.dataOrigin) return false
        if (id != pageAccessAudit.id) return false
        if (version != pageAccessAudit.version) return false
        if (vpdiCode != pageAccessAudit.vpdiCode) return false

        return true

    }

    @Override
    public String toString() {
        return """\
            PageAccessAudit{
                auditTime = $auditTime,
                loginId=$loginId,
                pidm=$pidm,
                appId=$appId,
                pageUrl=$pageUrl,                
                ipAddress=$ipAddress, 
                activityDate=$lastModified,               
                userId='$lastModifiedBy',                
                dataOrigin='$dataOrigin',
                id=$id,
                version=$version,
                vpdiCode=$vpdiCode 
            }"""
    }

    public static PageAccessAudit fetchByLoginId(String loginId) {
        PageAccessAudit pageAccessAudit
        if (loginId) {
            pageAccessAudit = PageAccessAudit.withSession { session ->
                pageAccessAudit = session.getNamedQuery('PageAccessAudit.fetchByLoginId').setString('loginId',loginId).uniqueResult()
            }
        }
        return pageAccessAudit
    }

    public static PageAccessAudit fetchByAppId(String appId) {
        PageAccessAudit pageAccessAudit
        if (appId) {
            pageAccessAudit = PageAccessAudit.withSession { session ->
                pageAccessAudit = session.getNamedQuery('PageAccessAudit.fetchByAppId').setString('appId',appId).uniqueResult()
            }
        }
        return pageAccessAudit
    }
}
