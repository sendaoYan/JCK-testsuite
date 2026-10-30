/*
 * @test
 * @summary JLS 14.12: the expression of a while statement must have type boolean
 *          or Boolean.
 * @compile/fail WhileNonBooleanFail.java
 */

class WhileNonBooleanFail {
    void m() {
        while (1) {}   // error: int cannot be converted to boolean
    }
}
