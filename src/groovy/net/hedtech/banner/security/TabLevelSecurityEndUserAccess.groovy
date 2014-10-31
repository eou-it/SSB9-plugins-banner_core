package net.hedtech.banner.security

/**
 */
public enum TabLevelSecurityEndUserAccess {
    FULL("F"),READONLY("Q"),HIDDEN("N");

    private String indicatorCode = null

    private TabLevelSecurityEndUserAccess(String indicatorCode) {
        this.indicatorCode = indicatorCode
    }

    public String getCode () {
        return indicatorCode;
    }

    public static getTabLevelSecurityAccessIndicator(String indicatorCode) {
        if ("F".equalsIgnoreCase(indicatorCode)) {
            return FULL
        } else if ("Q".equalsIgnoreCase(indicatorCode)) {
            return READONLY
        } else if ("N".equalsIgnoreCase(indicatorCode)) {
            return HIDDEN
        } else {
            throw new IllegalArgumentException("No Tab Level Security End User Access Indicator for the code $indicatorCode")
        }
    }

}


