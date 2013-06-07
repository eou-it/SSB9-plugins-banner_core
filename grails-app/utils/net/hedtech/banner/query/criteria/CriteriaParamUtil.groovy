package net.hedtech.banner.query.criteria


class CriteriaParamUtil {
    public static getValue(Object param) {
        if(param instanceof CriteriaParam) {
            return param.data
        }
        return param
    }

    public static setValue(Object param, Object value) {
        if(param instanceof CriteriaParam) {
            param.data = value;
        }
    }
}
