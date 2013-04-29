package net.hedtech.banner.newquery.operators

class CriteriaOperatorFactory {

    private CriteriaOperatorFactory() {

    }

    public static CriteriaOperator getCriteriaOperator(String key) {
        switch (key) {
            case Operators.EQUALS:
                return new EqualsOperator();
                break;
            case Operators.BETWEEN:
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
