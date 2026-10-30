/*
 * @test
 * @summary JLS 16.2.10: a while statement whose condition is the constant false
 *          never executes its body, so a variable assigned only in the body is not
 *          definitely assigned after the statement.
 * @compile/fail WhileConstantFalseFail.java
 */

class WhileConstantFalseFail {
    void m() {
        int x;
        while (false) x = 1;
        int y = x;   // error: variable x might not have been initialized
    }
}
