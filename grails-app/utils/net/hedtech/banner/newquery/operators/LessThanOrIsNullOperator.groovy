package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class LessThanOrIsNullOperator extends CriteriaOperator {
    public LessThanOrIsNullOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.lessthanorisnull";
        this.operator = "<";
        this.key = Operators.LESS_THAN_OR_IS_NULL
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
           CriteriaParam param = data.params.get(0);

           if(param.data instanceof Date) {
              return new DateLessThanOrIsNullOperator().getQueryString(data)
           }
           return "${data.tableAlias}.${data.tableBindingAttribute} < :${data.paramKey} OR ${data.tableAlias}.${data.tableBindingAttribute} IS NULL"
       }
       return ""
    }
}