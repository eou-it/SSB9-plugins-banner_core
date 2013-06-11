package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.query.criteria.DateQueryBuilder

class DateLessEqualsOrIsNullOperator extends LessEqualsOrIsNullOperator {
    public String getQueryString(CriteriaData data) {
       if(ifParamsExists(data)) {
           CriteriaParam param = data.params.get(0);

          boolean timeEntered = isTimeEntered(param)
         /* if(param.getAttribute("timeEntered") && true.equals(param.getAttribute("timeEntered"))) {
              timeEntered = true
          }*/

          DateQueryBuilder dateQueryBuilder = new DateQueryBuilder()
          if(timeEntered) {
              dateQueryBuilder.appendDateTimeSupport("${data.tableAlias}.${data.tableBindingAttribute}")
              dateQueryBuilder.append(" <= ")
              dateQueryBuilder.appendDateTimeSupport(":${param.paramKey}")

          } else {
              dateQueryBuilder.appendDateSupport("${data.tableAlias}.${data.tableBindingAttribute}")
              dateQueryBuilder.append(" <= ")
              dateQueryBuilder.appendDateSupport(":${param.paramKey}")
          }
          dateQueryBuilder.append(" OR ")
          dateQueryBuilder.append("${data.tableAlias}.${data.tableBindingAttribute}")
          dateQueryBuilder.append(" IS NULL ")
          return dateQueryBuilder.toString();
       }
       return ""
    }
}
