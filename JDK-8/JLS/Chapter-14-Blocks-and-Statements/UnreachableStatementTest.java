/*
 * @test
 * @summary JLS 14.21 (Java SE 8): a statement that cannot be reached is a
 *          compile-time error.
 * @compile/fail UnreachableStatementTest.java
 */

class UnreachableStatementTest {
    void m() {
        return;
        System.out.println("x");   // error: unreachable statement
    }
}
