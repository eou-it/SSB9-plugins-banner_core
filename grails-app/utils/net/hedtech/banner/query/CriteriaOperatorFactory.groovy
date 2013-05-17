package net.hedtech.banner.query

import net.hedtech.banner.query.operators.EqualsOperator
import net.hedtech.banner.query.operators.BetweenOperator
import net.hedtech.banner.query.operators.ContainsOperator
import net.hedtech.banner.query.operators.InOperator
import net.hedtech.banner.query.operators.EqualsIgnoreCaseOperator
import net.hedtech.banner.query.operators.EqualsOrIsNullOperator
import net.hedtech.banner.query.operators.StartsWithOperator
import net.hedtech.banner.query.operators.EndsWithOperator
import net.hedtech.banner.query.operators.NotEqualsIgnoreCaseOperator
import net.hedtech.banner.query.operators.NotEqualsOrIsNullOperator
import net.hedtech.banner.query.operators.IsNullOperator
import net.hedtech.banner.query.operators.IsNotNullOperator
import net.hedtech.banner.query.operators.NotEqualsOperator
import net.hedtech.banner.query.operators.GreaterThanOperator
import net.hedtech.banner.query.operators.GreaterThanEqualsOperator
import net.hedtech.banner.query.operators.LessThanOperator
import net.hedtech.banner.query.operators.LessThanOrIsNullOperator
import net.hedtech.banner.query.operators.LessThanEqualsOperator
import net.hedtech.banner.query.operators.LessEqualsOrIsNullOperator;
import net.hedtech.banner.query.operators.Operators;
import net.hedtech.banner.query.operators.CriteriaOperator;

class CriteriaOperatorFactory {

    private CriteriaOperatorFactory() {

    }

    public static CriteriaOperator getCriteriaOperator(String key) {
        switch (key) {
            case Operators.EQUALS:
                return new EqualsOperator();
                break;
            case Operators.BETWEEN:
            case "numericbetween": //TODO need to remove this when numeric between is removed from all zuls
                return new BetweenOperator();
                break;
            case Operators.CONTAINS:
                return new ContainsOperator();
                break;
            case Operators.IN:
                return new InOperator();
                break;
            case Operators.EQUALS_IGNORE_CASE:
                return new EqualsIgnoreCaseOperator();
                break;
            case Operators.EQUALS_OR_IS_NULL:
                return new EqualsOrIsNullOperator()
                break;
            case Operators.STARTS_WITH:
                return new StartsWithOperator()
                break;
            case Operators.ENDS_WITH:
                return new EndsWithOperator()
                break;
            case Operators.NOT_EQUALS_IGNORE_CASE:
                return new NotEqualsIgnoreCaseOperator()
                break;
            case Operators.NOT_EQUALS_OR_IS_NULL:
                return new NotEqualsOrIsNullOperator()
                break;
            case Operators.IS_NULL:
                return new IsNullOperator()
                break;
            case Operators.IS_NOT_NULL:
                return new IsNotNullOperator()
                break;
            case Operators.NOT_EQUALS:
                return new NotEqualsOperator()
                break;
            case Operators.GREATER_THAN:
                return new GreaterThanOperator()
                break;
            case Operators.GREATER_THAN_EQUALS:
                return new GreaterThanEqualsOperator()
                break;
            case Operators.LESS_THAN:
                return new LessThanOperator()
                break;
            case Operators.LESS_THAN_OR_IS_NULL:
                return new LessThanOrIsNullOperator()
                break;
            case Operators.LESS_THAN_EQUALS:
                return new LessThanEqualsOperator()
                break;
            case Operators.LESS_THAN_EQUALS_OR_IS_NULL:
                return new LessEqualsOrIsNullOperator()
                break;
        }
    }

    static final operatorGroups = [alphanumeric:[
            [operator:Operators.CONTAINS, default:"true"],
            [operator:Operators.STARTS_WITH],
            [operator:Operators.ENDS_WITH],
            [operator:Operators.EQUALS_IGNORE_CASE],
            [operator:Operators.NOT_EQUALS_IGNORE_CASE],
            [operator:Operators.NOT_EQUALS_OR_IS_NULL],
            [operator:Operators.IS_NULL],
            [operator:Operators.IS_NOT_NULL]
    ],
            numeric:[
                    [operator:Operators.EQUALS, default:"true"],
                    [operator:Operators.NOT_EQUALS],
                    [operator:Operators.NOT_EQUALS_OR_IS_NULL],
                    [operator:Operators.BETWEEN],
                    [operator:Operators.GREATER_THAN],
                    [operator:Operators.GREATER_THAN_EQUALS],
                    [operator:Operators.LESS_THAN],
                    [operator:Operators.LESS_THAN_OR_IS_NULL],
                    [operator:Operators.LESS_THAN_EQUALS],
                    [operator:Operators.LESS_THAN_EQUALS_OR_IS_NULL],
                    [operator:Operators.IS_NULL],
                    [operator:Operators.IS_NOT_NULL]
            ],
            date:[
                    [operator:Operators.EQUALS, default:"true"],
                    [operator:Operators.BETWEEN],
                    [operator:Operators.IN],
                    [operator:Operators.GREATER_THAN],
                    [operator:Operators.GREATER_THAN_EQUALS],
                    [operator:Operators.LESS_THAN],
                    [operator:Operators.LESS_THAN_OR_IS_NULL],
                    [operator:Operators.LESS_THAN_EQUALS],
                    [operator:Operators.LESS_THAN_EQUALS_OR_IS_NULL],
                    [operator:Operators.IS_NULL],
                    [operator:Operators.IS_NOT_NULL]
            ],
            boolean:[
                    [operator:Operators.EQUALS, default:"true"]
            ],
            checkbox:[
                    [operator:Operators.EQUALS_OR_IS_NULL, default:"true"]
            ],
            radio:[
                    [operator:Operators.EQUALS_OR_IS_NULL, default:"true"]
            ]
    ]
}
