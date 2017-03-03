/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.testing

import org.hibernate.annotations.Type
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.Temporal
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.GenerationType
import javax.persistence.TemporalType
import javax.persistence.ManyToOne
import javax.persistence.GeneratedValue
import javax.persistence.Version
import javax.persistence.SequenceGenerator

/**
 * Common Matching Source Code Rules Table
 */
@Entity
@Table(name = "GV_GORCMSC")
class CommonMatchingSourceRuleForTesting implements Serializable {

    /**
     * Surrogate ID for GORCMSC
     */
    @Id
    @Column(name="GORCMSC_SURROGATE_ID")
    @SequenceGenerator(name ="GORCMSC_SEQ_GEN", allocationSize =1, sequenceName  ="GORCMSC_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy =GenerationType.SEQUENCE, generator ="GORCMSC_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORCMSC
     */
    @Version
    @Column(name = "GORCMSC_VERSION",nullable = false, precision = 19)
    Long version

    /**
     * ON-LINE INDICATOR: Y/N indicates if the Source Code and associated rules (table gorcmsr) will be used for Common Matching when run on-line from a form.
     */
    @Type(type = "yes_no")
    @Column(name = "GORCMSC_ONLINE_MATCH_IND")
    Boolean onlineMatchIndicator

    /**
     * ENTITY INDICATOR: Identifies if the Source will be matching person (P), non-person (C), or both (B) entity types when selecting SPRIDEN records.
     */
    @Column(name = "GORCMSC_ENTITY_CDE")
    String entity

    /**
     * TRANSPOSE DATE INDICATOR: Option allows birthdate day/month to also be checked as month/day. Valid values are Y to allow date transposing or N to disallow date transposing. Default is N.
     */
    @Type(type = "yes_no")
    @Column(name = "GORCMSC_TRANSPOSE_DATE_IND")
    Boolean transposeDateIndicator

    /**
     * TRANSPOSE NAME INDICATOR: Option allows First Name/Last Name to also be checked as Last Name/First Name. Valid values are Y to allow name transposing or N to disallow transposing. Default is N.
     */
    @Type(type = "yes_no")
    @Column(name = "GORCMSC_TRANSPOSE_NAME_IND")
    Boolean transposeNameIndicator

    /**
     * ALIAS WILDCARD INDICATOR: Option allows wildcard searching when performing name alias look ups. Valid values are Y to allow wildcard searching or N to disallow wildcard searching. Default is N.
     */
    @Type(type = "yes_no")
    @Column(name = "GORCMSC_ALIAS_WILDCARD_IND")
    Boolean aliasWildcardIndicator

    /**
     * LENGTH OVERRIDE INDICATOR: Option allows data length to override rule length if length entered is less than rule length. Valid values are Y to override or N to not override. Default is N.
     */
    @Type(type = "yes_no")
    @Column(name = "GORCMSC_LENGTH_OVERRIDE_IND")
    Boolean lengthOverrideIndicator

    /**
     * API FAILURE INDICATOR: Option prevents creation of ID if address, phone, or email api fail during the Create ID process. Y prevents the creation fo the ID. Default is N.
     */
    @Type(type = "yes_no")
    @Column(name = "GORCMSC_API_FAILURE_IND")
    Boolean apiFailureIndicator

    /**
     * ACTIVITY DATE: Date record was created or last updated.
     */
    @Column(name = "GORCMSC_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: User ID of the User who created or last updated the record.
     */
    @Column(name = "GORCMSC_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "GORCMSC_DATA_ORIGIN")
    String dataOrigin


    /**
     * Foreign Key : FKV_GORCMSC_INV_GTVCMSC_CODE
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name="GORCMSC_CMSC_CODE", referencedColumnName="GTVCMSC_CODE")
    ])
    CommonMatchingSourceForTesting commonMatchingSource

    /**
     * Foreign Key : FKV_GORCMSC_INV_STVATYP_CODE
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name="GORCMSC_ATYP_CODE", referencedColumnName="STVATYP_CODE")
    ])
    AddressTypeForTesting addressType

    /**
     * Foreign Key : FKV_GORCMSC_INV_STVTELE_CODE
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name="GORCMSC_TELE_CODE", referencedColumnName="STVTELE_CODE")
    ])
    TelephoneTypeForTesting telephoneType

    /**
     * Foreign Key : FKV_GORCMSC_INV_GTVEMAL_CODE
     */
    @ManyToOne
    @JoinColumns([
            @JoinColumn(name="GORCMSC_EMAL_CODE", referencedColumnName="GTVEMAL_CODE")
    ])
    EmailTypeForTesting emailType


    public String toString() {
        """CommonMatchingSourceRule[
					id=$id,
					version=$version,
					onlineMatchIndicator=$onlineMatchIndicator,
					entity=$entity,
					transposeDateIndicator=$transposeDateIndicator,
					transposeNameIndicator=$transposeNameIndicator,
					aliasWildcardIndicator=$aliasWildcardIndicator,
					lengthOverrideIndicator=$lengthOverrideIndicator,
					apiFailureIndicator=$apiFailureIndicator,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					commonMatchingSource=$commonMatchingSource,
					addressType=$addressType,
					telephoneType=$telephoneType,
					emailType=$emailType]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof CommonMatchingSourceRuleForTesting)) return false
        CommonMatchingSourceRuleForTesting that = (CommonMatchingSourceRuleForTesting) o
        if(id != that.id) return false
        if(version != that.version) return false
        if(onlineMatchIndicator != that.onlineMatchIndicator) return false
        if(entity != that.entity) return false
        if(transposeDateIndicator != that.transposeDateIndicator) return false
        if(transposeNameIndicator != that.transposeNameIndicator) return false
        if(aliasWildcardIndicator != that.aliasWildcardIndicator) return false
        if(lengthOverrideIndicator != that.lengthOverrideIndicator) return false
        if(apiFailureIndicator != that.apiFailureIndicator) return false
        if(lastModified != that.lastModified) return false
        if(lastModifiedBy != that.lastModifiedBy) return false
        if(dataOrigin != that.dataOrigin) return false
        if(commonMatchingSource != that.commonMatchingSource) return false
        if(addressType != that.addressType) return false
        if(telephoneType != that.telephoneType) return false
        if(emailType != that.emailType) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (onlineMatchIndicator != null ? onlineMatchIndicator.hashCode() : 0)
        result = 31 * result + (entity != null ? entity.hashCode() : 0)
        result = 31 * result + (transposeDateIndicator != null ? transposeDateIndicator.hashCode() : 0)
        result = 31 * result + (transposeNameIndicator != null ? transposeNameIndicator.hashCode() : 0)
        result = 31 * result + (aliasWildcardIndicator != null ? aliasWildcardIndicator.hashCode() : 0)
        result = 31 * result + (lengthOverrideIndicator != null ? lengthOverrideIndicator.hashCode() : 0)
        result = 31 * result + (apiFailureIndicator != null ? apiFailureIndicator.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (commonMatchingSource != null ? commonMatchingSource.hashCode() : 0)
        result = 31 * result + (addressType != null ? addressType.hashCode() : 0)
        result = 31 * result + (telephoneType != null ? telephoneType.hashCode() : 0)
        result = 31 * result + (emailType != null ? emailType.hashCode() : 0)
        return result
    }

    static constraints = {
        onlineMatchIndicator(nullable:false)
        entity(nullable:false, maxSize:1, inList:["P","C","B"])
        transposeDateIndicator(nullable:false)
        transposeNameIndicator(nullable:false)
        aliasWildcardIndicator(nullable:false)
        lengthOverrideIndicator(nullable:false)
        apiFailureIndicator(nullable:false)
        lastModified(nullable:true)
        lastModifiedBy(nullable:true, maxSize:30)
        dataOrigin(nullable:true, maxSize:30)
        commonMatchingSource(nullable:false)
        addressType(nullable:true)
        telephoneType(nullable:true)
        emailType(nullable:true)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = [ 'commonMatchingSource' ]
}
