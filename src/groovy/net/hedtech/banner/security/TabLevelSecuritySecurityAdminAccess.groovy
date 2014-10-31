package net.hedtech.banner.security

/**
 */
public enum TabLevelSecuritySecurityAdminAccess {
    NO_PRIVILEGES("F"), NO_PRIVILEGE_TO_HIDE("Q"), ALL_PRIVILEGES("N");

    private String indicatorCode = null

    private TabLevelSecuritySecurityAdminAccess(String indicatorCode) {
        this.indicatorCode = indicatorCode
    }

    public String getCode () {
        return indicatorCode;
    }

    public static getTabLevelSecurityAccessIndicator(String indicatorCode) {
        if ("F".equalsIgnoreCase(indicatorCode)) {
            return NO_PRIVILEGES
        } else if ("Q".equalsIgnoreCase(indicatorCode)) {
            return NO_PRIVILEGE_TO_HIDE
        } else if ("N".equalsIgnoreCase(indicatorCode)) {
            return ALL_PRIVILEGES
        } else {
            throw new IllegalArgumentException("No Tab Level Security Admin Access Indicator for the code $indicatorCode")
        }
    }

}


