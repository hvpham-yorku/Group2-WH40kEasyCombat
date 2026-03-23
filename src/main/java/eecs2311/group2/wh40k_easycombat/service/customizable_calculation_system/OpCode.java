package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

public enum OpCode {
    PUSH_CONST,
    LOAD,
    STORE,
    POP,

    // Unary Expr
    NEG,
    POS,
    NOT,

    // Binary Eval
    AND,
    OR,
    ADD,
    SUB,
    MUL,
    DIV,
    CMP_GT,
    CMP_GE,
    CMP_EQ,
    CMP_NE,
    CMP_LT,
    CMP_LE,

    JMP,
    JMP_IF_FALSE,
    GOTO,

    EVAL_EXPR,

    // game rule instructions
    ROLL,
    ROLL_POOL,
    FILTER_POOL,
    REROLL_POOL,
    COUNT_POOL,
}