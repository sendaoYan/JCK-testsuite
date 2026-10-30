/*
 * @test
 * @summary JLS 14.22: a statement that cannot be executed is a compile-time
 *          error; the body of while (false) is unreachable.
 * @compile/fail UnreachableWhileFail.java
 */

class UnreachableWhileFail {
    void m() {
        while (false) { int x = 3; }   // error: unreachable statement
    }
}
