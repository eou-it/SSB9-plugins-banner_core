package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class IsNotNullOperator extends CriteriaOperator {

    public IsNotNullOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.isnotnull";
        this.operator = "is not null";
        this.key = Operators.IS_NOT_NULL
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param = data.params.get(0);

            return "${data.tableAlias}.${data.tableBindingAttribute} IS NOT NULL"
        }
        return ""
    }
}
