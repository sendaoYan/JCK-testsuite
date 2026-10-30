/*
 * @test
 * @summary JLS 14.8: a parenthesized expression is not a statement expression and
 *          so may not be used as an expression statement.
 * @compile/fail ExpressionStatementIllegalFail.java
 */

class ExpressionStatementIllegalFail {
    void m() {
        (System.out.println("x"));   // error: not a statement
    }
}
