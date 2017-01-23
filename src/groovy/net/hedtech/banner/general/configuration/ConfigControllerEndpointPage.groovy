/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GURCTLEPP database table.
 *
 */
@Entity
@Table(name = 'GURCTLEPP', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigControllerEndpointPage.findAll',
                    query = '''SELECT ccep FROM ConfigControllerEndpointPage ccep'''),
        @NamedQuery(name = 'ConfigControllerEndpointPage.getAllConfigByAppName',
                    query = '''SELECT new net.hedtech.banner.general.configuration.RequestURLMap(ccep.pageName, crpm.roleCode, capp.appName,
                                    ccep.displaySequence, ccep.pageId, ccep.gubapplAppId, ccep.version)
                                FROM ConfigControllerEndpointPage ccep, ConfigRolePageMapping crpm,
                                   ConfigApplication capp
                                WHERE (ccep.gubapplAppId = crpm.gubapplAppId AND ccep.pageId = crpm.pageId)
                                AND (ccep.gubapplAppId = capp.appId)
                                AND capp.appName = :appName''')
])
public class ConfigControllerEndpointPage implements Serializable {
    private static final long serialVersionUID = 1L

    @Column(name = 'GURCTLEPP_SURROGATE_ID', precision = 19)
    Long id

    @Temporal(TemporalType.DATE)
    @Column(name = 'GURCTLEPP_ACTIVITY_DATE', nullable = false)
    Date activityDate

    @Column(name = 'GURCTLEPP_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GURCTLEPP_DESCRIPTION', length = 256)
    String description

    @Column(name = 'GURCTLEPP_DISPLAY_SEQUENCE', precision = 19)
    Long displaySequence

    @Column(name = 'GURCTLEPP_ENABLE_DISABLE', length = 256)
    String enableDisable

    @Column(name = 'GURCTLEPP_GUBAPPL_APP_ID', nullable = false, precision = 19)
    Long gubapplAppId

    @Column(name = 'GURCTLEPP_PAGE_NAME', length = 256)
    String pageName

    @Version
    @Column(name = 'GURCTLEPP_USER_ID', length = 30)
    String userId

    @Column(name = 'GURCTLEPP_VERSION', precision = 19)
    Long version

    @Column(name = 'GURCTLEPP_VPDI_CODE', length = 6)
    String vpdiCode

    @Column(name = 'PAGE_ID', nullable = false, precision = 19)
    Long pageId

    public ConfigControllerEndpointPage() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigControllerEndpointPage gurctlepp = (ConfigControllerEndpointPage) o

        if (activityDate != gurctlepp.activityDate) return false
        if (gubapplAppId != gurctlepp.gubapplAppId) return false
        if (dataOrigin != gurctlepp.dataOrigin) return false
        if (description != gurctlepp.description) return false
        if (displaySequence != gurctlepp.displaySequence) return false
        if (enableDisable != gurctlepp.enableDisable) return false
        if (id != gurctlepp.id) return false
        if (pageId != gurctlepp.pageId) return false
        if (pageName != gurctlepp.pageName) return false
        if (userId != gurctlepp.userId) return false
        if (version != gurctlepp.version) return false
        if (vpdiCode != gurctlepp.vpdiCode) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (activityDate != null ? activityDate.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (displaySequence != null ? displaySequence.hashCode() : 0)
        result = 31 * result + (enableDisable != null ? enableDisable.hashCode() : 0)
        result = 31 * result + (gubapplAppId != null ? gubapplAppId.hashCode() : 0)
        result = 31 * result + (pageName != null ? pageName.hashCode() : 0)
        result = 31 * result + (userId != null ? userId.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (vpdiCode != null ? vpdiCode.hashCode() : 0)
        result = 31 * result + (pageId != null ? pageId.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            ConfigControllerEndpointPage{
                id=$id,
                activityDate=$activityDate,
                dataOrigin='$dataOrigin',
                description='$description',
                displaySequence=$displaySequence,
                enableDisable='$enableDisable',
                gubapplAppId=$gubapplAppId,
                pageName='$pageName',
                userId='$userId',
                version=$version,
                vpdiCode='$vpdiCode',
                pageId=$pageId
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def findAll() {
        def configRolePageMapping
        ConfigControllerEndpointPage.withSession { session ->
            configRolePageMapping = session.getNamedQuery('ConfigControllerEndpointPage.findAll').list()
        }
        return configRolePageMapping
    }

    /**
     * Named query to fetch all data from this domain by appName.
     * @param appName String
     * @return list of RequestURLMap.
     */
    public static def getAllConfigByAppName(def appName) {
        def configRolePageMapping
        configRolePageMapping = ConfigControllerEndpointPage.withSession { session ->
            configRolePageMapping = session.getNamedQuery('ConfigControllerEndpointPage.getAllConfigByAppName').setString('appName', appName).list()
        }
        return configRolePageMapping
    }
}
