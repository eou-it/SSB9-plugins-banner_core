package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.Query
import net.hedtech.banner.query.criteria.CriteriaParam

abstract class CriteriaOperator {
    protected String label
    protected String operator
    protected String key

    public Object formatValue(Object value) {
        return value;
    }

    //public abstract Query getQuery(CriteriaData data)
    public Query getQuery(CriteriaData data) {
        return Query.createQuery(getQueryString(data))
    }

    public boolean ifParamsExists(CriteriaData data) {
        if(data.params) {
            return !data.params.isEmpty()
        }
        return false
    }

    public boolean isTimeEntered(CriteriaParam param) {
        boolean timeEntered = false
        if(param.getAttribute("timeEntered") && true.equals(param.getAttribute("timeEntered"))) {
            timeEntered = true
        }
        return timeEntered
    }

    public abstract getQueryString(CriteriaData data)

}
