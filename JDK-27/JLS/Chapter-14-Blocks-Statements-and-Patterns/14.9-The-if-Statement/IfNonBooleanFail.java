/*
 * @test
 * @summary JLS 14.9: the expression of an if statement must have type boolean
 *          or Boolean.
 * @compile/fail IfNonBooleanFail.java
 */

class IfNonBooleanFail {
    void m() {
        if (1) {}   // error: incompatible types: int cannot be converted to boolean
    }
}
