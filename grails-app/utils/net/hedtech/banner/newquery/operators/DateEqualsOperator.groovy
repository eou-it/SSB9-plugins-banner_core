package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam
import net.hedtech.banner.newquery.DateQueryBuilder

class DateEqualsOperator extends EqualsOperator{
    public String getQueryString(CriteriaData data) {
        if(ifParamsExists(data)) {
            CriteriaParam param = data.params.get(0);
            boolean timeEntered = isTimeEntered(param)
            /*if(param.getAttribute("timeEntered") && true.equals(param.getAttribute("timeEntered"))) {
                timeEntered = true
            }*/

            DateQueryBuilder dateQueryBuilder = new DateQueryBuilder()
            if(timeEntered) {
                dateQueryBuilder.appendDateTimeSupport("${data.tableAlias}.${data.tableBindingAttribute}")
                dateQueryBuilder.append(" = ")
                dateQueryBuilder.appendDateTimeSupport(":${param.paramKey}")
            } else {
                dateQueryBuilder.appendDateSupport("${data.tableAlias}.${data.tableBindingAttribute}")
                dateQueryBuilder.append(" = ")
                dateQueryBuilder.appendDateSupport(":${param.paramKey}")
            }
            return dateQueryBuilder.toString();
        }
        return ""
    }
}
