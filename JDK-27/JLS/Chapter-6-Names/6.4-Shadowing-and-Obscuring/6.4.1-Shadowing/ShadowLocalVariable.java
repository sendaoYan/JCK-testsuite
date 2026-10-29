/*
 * @test
 * @summary JLS 6.4 Example 6.4-1 Test1: a local variable may not be redeclared
 *          within its own scope (int i; for (int i = 0; ...) is an error).
 * @compile/fail ShadowLocalVariable.java
 */

class ShadowLocalVariable {
    void m() {
        int i;
        for (int i = 0; i < 10; i++) {   // error: i already declared
        }
    }
}
