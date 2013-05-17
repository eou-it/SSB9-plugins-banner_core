package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class IsNullOperator extends CriteriaOperator {

    public IsNullOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.isnull";
        this.operator = "is null";
        this.key = Operators.IS_NULL
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param = data.params.get(0);

            return "${data.tableAlias}.${data.tableBindingAttribute} IS NULL"
        }
        return ""
    }
}
