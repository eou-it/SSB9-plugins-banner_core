/** *****************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.testing

import org.hibernate.annotations.Type
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.Temporal
import javax.persistence.GenerationType
import javax.persistence.TemporalType
import javax.persistence.GeneratedValue
import javax.persistence.Version
import javax.persistence.SequenceGenerator

/**
 * The GTVEMAL table contains the valid e-mail type codes.
 */

@Entity
@Table(name = "GTVEMAL")
class EmailTypeForTesting implements Serializable {

    /**
     * Surrogate ID for GTVEMAL
     */
    @Id
    @Column(name = "GTVEMAL_SURROGATE_ID")
    @SequenceGenerator(name = "GTVEMAL_SEQ_GEN", allocationSize = 1, sequenceName = "GTVEMAL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GTVEMAL_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GTVEMAL
     */
    @Version
    @Column(name = "GTVEMAL_VERSION", nullable = false, precision = 19)
    Long version

    /**
     * A code identifying the type of e-mail address.
     */
    @Column(name = "GTVEMAL_CODE", nullable = false, unique = true, length = 4)
    String code

    /**
     * The description of the e-mail address.
     */
    @Column(name = "GTVEMAL_DESC", length = 60)
    String description

    /**
     * Display on Web Indicator, Y indicates this type of email should display
     */
    @Type(type = "yes_no")
    @Column(name = "GTVEMAL_DISP_WEB_IND", nullable = false, length = 1)
    Boolean displayWebIndicator

    /**
     * URL Indicator, Y indicates that this email record is actually a URL.
     */
    @Type(type = "yes_no")
    @Column(name = "GTVEMAL_URL_IND", nullable = false, length = 1)
    Boolean urlIndicator

    /**
     * The date on which the row was added or modified.
     */
    @Column(name = "GTVEMAL_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * Last modified by column for GTVEMAL
     */
    @Column(name = "GTVEMAL_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data origin column for GTVEMAL
     */
    @Column(name = "GTVEMAL_DATA_ORIGIN", length = 30)
    String dataOrigin



    public String toString() {
        """EmailType[
					id=$id,
					version=$version,
					code=$code,
					description=$description,
					displayWebIndicator=$displayWebIndicator,
					urlIndicator=$urlIndicator,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof EmailTypeForTesting)) return false
        EmailTypeForTesting that = (EmailTypeForTesting) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (code != that.code) return false
        if (description != that.description) return false
        if (displayWebIndicator != that.displayWebIndicator) return false
        if (urlIndicator != that.urlIndicator) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (code != null ? code.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (displayWebIndicator != null ? displayWebIndicator.hashCode() : 0)
        result = 31 * result + (urlIndicator != null ? urlIndicator.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        return result
    }


    static constraints = {
        code(nullable: false, maxSize: 4)
        description(nullable: true, maxSize: 60)
        displayWebIndicator(nullable: false)
        urlIndicator(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['code']
}
