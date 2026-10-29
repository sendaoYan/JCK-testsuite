/*
 * @test
 * @summary JLS 6.4 Example 6.4-1 Test5: a pattern variable may not shadow a
 *          local variable, including another pattern variable.
 * @compile/fail PatternVariableShadowsLocal.java
 */

class Point { int x, y; }

class PatternVariableShadowsLocal {
    void m(Object a, Object b) {
        if (a instanceof Point p) {
            if (b instanceof Point p) {   // error: p already in scope
            }
        }
    }
}
