/*
 * @test
 * @summary JLS 14.21: a statement is unreachable if it cannot be executed; a
 *          statement following a return statement is a compile-time error.
 * @compile/fail UnreachableAfterReturnFail.java
 */

class UnreachableAfterReturnFail {
    void m() {
        return;
        int x = 1;   // error: unreachable statement
    }
}
