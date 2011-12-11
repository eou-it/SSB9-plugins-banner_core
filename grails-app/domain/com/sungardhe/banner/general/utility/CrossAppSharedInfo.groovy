package com.sungardhe.banner.general.utility

import javax.persistence.*

@Entity
@Table(name="ABCDEDF")
class CrossAppSharedInfo implements Serializable {

    @Id
    @Column(name = "HORCRSI_SURROGATE_ID")
    @SequenceGenerator(name = "HORCRSI_SEQ_GEN", allocationSize = 1, sequenceName = "HORCRSI_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "HORCRSI_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for HORCRSI
     */
    @Version
    @Column(name = "HORCRSI_VERSION", nullable = false, precision = 19)
    Long version


    @Column(name="HORCRSI_USER_NAME")
    String userName

    @Column(name="HORCRSI_APP_NAME")
    String appName

    @Column(name="HORCRSI_INFO_TYPE")
    String infoType

    /**
     * todo HOW TO SHARE NON-STRING DATA ???
     */
    @Column(name="HORCRSI_INFO")
    String info

    static constraints = {
    }


    boolean equals(o) {
        if (this.is(o)) return true;
        if (getClass() != o.class) return false;

        CrossAppSharedInfo that = (CrossAppSharedInfo) o;

        if (appName != that.appName) return false;
        if (info != that.info) return false;
        if (infoType != that.infoType) return false;
        if (userName != that.userName) return false;

        return true;
    }

    int hashCode() {
        int result;
        result = (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (appName != null ? appName.hashCode() : 0);
        result = 31 * result + (infoType != null ? infoType.hashCode() : 0);
        result = 31 * result + (info != null ? info.hashCode() : 0);
        return result;
    }


    public String toString ( ) {
        final StringBuilder sb = new StringBuilder ( ) ;
        sb . append ( "CrossAppSharedInfo" ) ;
        sb . append ( "{userName='" ) . append ( userName ) . append ( '\'' ) ;
        sb . append ( ", appName='" ) . append ( appName ) . append ( '\'' ) ;
        sb . append ( ", infoType='" ) . append ( infoType ) . append ( '\'' ) ;
        sb . append ( ", info='" ) . append ( info ) . append ( '\'' ) ;
        sb . append ( '}' ) ;
        return sb . toString ( ) ;
    }
}
