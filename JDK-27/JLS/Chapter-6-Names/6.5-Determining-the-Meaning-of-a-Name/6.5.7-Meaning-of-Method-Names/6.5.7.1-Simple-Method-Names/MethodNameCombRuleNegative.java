/*
 * @test
 * @summary JLS 6.5.7.1 Example 6.5.7.1-1: f2(0) is a compile-time error because
 *          Super.f2(String) shadows the search and is not applicable, and class
 *          Test is not searched.
 * @compile/fail MethodNameCombRuleNegative.java
 */

class Super {
    void f2(String s) {}
}

class MethodNameCombRuleNegative {
    void f2(int i) {}
    void m() {
        new Super() {
            { f2(0); }   // error: Super.f2(String) not applicable
        };
    }
}
