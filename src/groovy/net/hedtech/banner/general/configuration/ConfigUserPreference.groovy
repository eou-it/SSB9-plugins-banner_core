/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GURUCFG database table.
 *
 */
@Entity
@Table(name = 'GURUCFG', schema = 'GENERAL')
@NamedQuery(name = 'ConfigUserPreference.findAll', query = '''SELECT g FROM ConfigUserPreference g''')
public class ConfigUserPreference implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @Column(name = 'GURUCFG_SURROGATE_ID', precision = 19)
    BigDecimal id

    @Column(name = 'CONFIG_NAME', length = 50)
    String configName

    @Temporal(TemporalType.DATE)
    @Column(name = 'GURUCFG_ACTIVITY_DATE', nullable = false)
    Date activityDate

    @Column(name = 'GURUCFG_CONFIG_TYPE', length = 30)
    String configType

    @Lob
    @Column(name = 'GURUCFG_CONFIG_VALUE')
    String configValue

    @Column(name = 'GURUCFG_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GURUCFG_GUBAPPL_APP_ID', nullable = false, precision = 19)
    BigDecimal gubapplAppId

    @Column(name = 'GURUCFG_PIDM', precision = 19)
    BigDecimal pidm

    @Column(name = 'GURUCFG_USER_ID', length = 30)
    String userId

    @Version
    @Column(name = 'GURUCFG_VERSION', precision = 19)
    BigDecimal version

    @Column(name = 'GURUCFG_VPDI_CODE', length = 6)
    String vpdiCode

    public ConfigUserPreference() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigUserPreference gurucfg = (ConfigUserPreference) o

        if (activityDate != gurucfg.activityDate) return false
        if (configName != gurucfg.configName) return false
        if (configType != gurucfg.configType) return false
        if (configValue != gurucfg.configValue) return false
        if (dataOrigin != gurucfg.dataOrigin) return false
        if (gubapplAppId != gurucfg.gubapplAppId) return false
        if (id != gurucfg.id) return false
        if (pidm != gurucfg.pidm) return false
        if (userId != gurucfg.userId) return false
        if (version != gurucfg.version) return false
        if (vpdiCode != gurucfg.vpdiCode) return false

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
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (userId != null ? userId.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (vpdiCode != null ? vpdiCode.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            ConfigUserPreference{
                id=$id,
                configName='$configName',
                activityDate=$activityDate,
                configType='$configType',
                configValue='$configValue',
                dataOrigin='$dataOrigin',
                gubapplAppId=$gubapplAppId,
                pidm=$pidm,
                userId='$userId',
                version=$version,
                vpdiCode='$vpdiCode'
            }"""
    }
}
