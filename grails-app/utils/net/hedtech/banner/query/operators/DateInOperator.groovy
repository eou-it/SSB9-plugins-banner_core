package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.query.criteria.DateQueryBuilder

class DateInOperator extends InOperator {
    public String getQueryString(CriteriaData data) {
        if(ifParamsExists(data)) {
            CriteriaParam param = data.params.get(0);

            List params = getParamsFromCriteriaParams(param.data)

            boolean timeEntered = isTimeEntered(param)

            DateQueryBuilder dateQueryBuilder = new DateQueryBuilder()
            if(timeEntered) {
                dateQueryBuilder.appendDateTimeSupport("${data.tableAlias}.${data.tableBindingAttribute}")
                dateQueryBuilder.append(" IN (")
                dateQueryBuilder.append(":${param.paramKey}")
                dateQueryBuilder.append(")")
            } else {
                dateQueryBuilder.appendDateSupport("${data.tableAlias}.${data.tableBindingAttribute}")
                dateQueryBuilder.append(" IN (")
                dateQueryBuilder.append(":${param.paramKey}")
                dateQueryBuilder.append(")")
            }
            return dateQueryBuilder.toString();
        }
        return ""
    }

    public boolean isTimeEntered(CriteriaParam param) {
       List criteriaParams = param.data
       CriteriaParam innerParam = criteriaParams.get(0)

       boolean timeEntered = super.isTimeEntered(innerParam);

      /* if(innerParam.getAttribute("timeEntered") && true.equals(innerParam.getAttribute("timeEntered"))) {
          timeEntered = true
       }*/
       return timeEntered
    }
}
