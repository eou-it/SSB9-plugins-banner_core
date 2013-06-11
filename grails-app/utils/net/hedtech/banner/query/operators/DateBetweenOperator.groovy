package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.query.criteria.DateQueryBuilder

class DateBetweenOperator extends BetweenOperator {
    public String getQueryString(CriteriaData data) {
        if(data.params.size() > 1) {
            CriteriaParam lhsParam = data.params.get(0);
            CriteriaParam rhsParam = data.params.get(1);

            boolean lhsTimeEntered = false
            boolean rhsTimeEntered = false

            //if(param1.getAttribute("timeEntered") && true.equals(param1.getAttribute("timeEntered"))) {
            lhsTimeEntered = isTimeEntered(lhsParam)
            //}
            //if(param2.getAttribute("timeEntered") && true.equals(param2.getAttribute("timeEntered"))) {
            rhsTimeEntered = isTimeEntered(rhsParam)
            //}
            DateQueryBuilder dateQueryBuilder = new DateQueryBuilder()
            if(lhsTimeEntered) {
                dateQueryBuilder.appendDateTimeSupport("${data.tableAlias}.${data.tableBindingAttribute}")
                dateQueryBuilder.append(" between ")
                dateQueryBuilder.appendDateTimeSupport(":${lhsParam.paramKey}")
                dateQueryBuilder.append(" and ")
                dateQueryBuilder.appendDateTimeSupport(":${rhsParam.paramKey}")
            } else {
                dateQueryBuilder.appendDateSupport("${data.tableAlias}.${data.tableBindingAttribute}")
                dateQueryBuilder.append(" between ")
                dateQueryBuilder.appendDateSupport(":${lhsParam.paramKey}")
                dateQueryBuilder.append(" and ")
                dateQueryBuilder.appendDateSupport(":${rhsParam.paramKey}")
            }
            return dateQueryBuilder.toString();
        }
        return ""
    }
}
