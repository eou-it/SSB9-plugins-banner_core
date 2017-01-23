/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUROCFG database table.
 *
 */
@Entity
@Table(name = 'GUROCFG', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigurationProperties.findAll', query = '''SELECT g FROM ConfigurationProperties g''')
])
public class ConfigurationProperties implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @Column(name = 'GUROCFG_SURROGATE_ID', precision = 19)
    Long id

    @Column(name = 'CONFIG_NAME', length = 50)
    String configName

    @Temporal(TemporalType.DATE)
    @Column(name = 'GUROCFG_ACTIVITY_DATE', nullable = false)
    Date activityDate

    @Column(name = 'GUROCFG_CONFIG_TYPE', length = 30)
    String configType

    @Lob
    @Column(name = 'GUROCFG_CONFIG_VALUE')
    String configValue

    @Column(name = 'GUROCFG_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GUROCFG_GUBAPPL_APP_ID', nullable = false, precision = 19)
    Long gubapplAppId

    @Column(name = 'GUROCFG_USER_ID', length = 30)
    String userId

    @Version
    @Column(name = 'GUROCFG_VERSION', precision = 19)
    Long version

    @Column(name = 'GUROCFG_VPDI_CODE', length = 6)
    String vpdiCode

    public ConfigurationProperties() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigurationProperties gurocfg = (ConfigurationProperties) o

        if (activityDate != gurocfg.activityDate) return false
        if (configName != gurocfg.configName) return false
        if (configType != gurocfg.configType) return false
        if (configValue != gurocfg.configValue) return false
        if (dataOrigin != gurocfg.dataOrigin) return false
        if (gubapplAppId != gurocfg.gubapplAppId) return false
        if (id != gurocfg.id) return false
        if (userId != gurocfg.userId) return false
        if (version != gurocfg.version) return false
        if (vpdiCode != gurocfg.vpdiCode) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (configName != null ? configName.hashCode() : 0)
        result = 31 * result + (activityDate != null ? activityDate.hashCode() : 0)
        result = 31 * result + (configType != null ? configType.hashCode() : 0)
        result = 31 * result + (configValue != null ? configValue.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (gubapplAppId != null ? gubapplAppId.hashCode() : 0)
        result = 31 * result + (userId != null ? userId.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (vpdiCode != null ? vpdiCode.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            ConfigurationProperties{
                id=$id,
                configName='$configName',
                activityDate=$activityDate,
                configType='$configType',
                configValue='$configValue',
                dataOrigin='$dataOrigin',
                gubapplAppId=$gubapplAppId,
                userId='$userId',
                version=$version,
                vpdiCode='$vpdiCode'
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def findAll() {
        def configurationProperties
        ConfigurationProperties.withSession { session ->
            configurationProperties = session.getNamedQuery('ConfigurationProperties.findAll').list()
        }
        return configurationProperties
    }
}
