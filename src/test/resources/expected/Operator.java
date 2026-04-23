package com.example.lang;

/**
 * Arithmetic operators with symbolic representation and evaluation logic.
 *
 * @since 1.0
 */
public enum Operator implements java.util.function.BinaryOperator {
    /**
     * Addition operator.
     */
    ADD("+", 1) {
        @Override
        public Object apply(Object a, Object b) {
            return (Object) (((Number) a).doubleValue() + ((Number) b).doubleValue());
        }
    },
    /**
     * Subtraction operator.
     */
    SUBTRACT("-", 1) {
        @Override
        public Object apply(Object a, Object b) {
            return (Object) (((Number) a).doubleValue() - ((Number) b).doubleValue());
        }
    },
    /**
     * Multiplication operator.
     */
    MULTIPLY("*", 2) {
        @Override
        public Object apply(Object a, Object b) {
            return (Object) (((Number) a).doubleValue() * ((Number) b).doubleValue());
        }
    },
    /**
     * Division operator.
     */
    DIVIDE("/", 2) {
        @Override
        public Object apply(Object a, Object b) {
            return (Object) (((Number) a).doubleValue() / ((Number) b).doubleValue());
        }
    };

    /**
     * The operator symbol.
     */
    private final String symbol;

    /**
     * The operator precedence level.
     */
    private final int precedence;

    /**
     * Constructs an operator.
     *
     * @param symbol the symbol
     * @param precedence the precedence
     */
    Operator(String symbol, int precedence) {
        this.symbol = symbol;
        this.precedence = precedence;
    }

    /**
     * Returns the operator symbol.
     *
     * @return the symbol string
     */
    public String symbol() {
        return symbol;
    }

    /**
     * Returns the operator precedence.
     *
     * @return the precedence level
     */
    public int precedence() {
        return precedence;
    }

    /**
     * Returns the operator for the given symbol.
     *
     * @return the matching operator
     * @throws IllegalArgumentException if no operator matches
     * @param symbol the operator symbol
     */
    public static Operator fromSymbol(String symbol) throws IllegalArgumentException {
        for (Operator op : values()) {
            if (op.symbol.equals(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operator: ".concat(symbol));
    }

    @Override
    public String toString() {
        return symbol;
    }
}
