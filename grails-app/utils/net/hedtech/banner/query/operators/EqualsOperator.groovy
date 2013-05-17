package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class EqualsOperator extends CriteriaOperator {

    public EqualsOperator () {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.equals";
        this.operator = "=";
        this.key = Operators.EQUALS
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param = data.params.get(0);

            if(param.data instanceof Date) {
                return new DateEqualsOperator().getQueryString(data)
            }
        }
        return "${data.tableAlias}.${data.tableBindingAttribute} = :${data.paramKey} "
    }
}
