/*
 * @test
 * @summary JLS 16.1.1 (Java SE 8): after "false && (x = 1)" the right operand is
 *          never evaluated, so x is not definitely assigned and a subsequent read
 *          is a compile-time error.
 * @compile/fail BooleanConstantFalseFail.java
 */

class BooleanConstantFalseFail {
    void m() {
        int x;
        boolean r = false && (x = 1) == 1;
        int y = x;   // error: variable x might not have been initialized
    }
}
