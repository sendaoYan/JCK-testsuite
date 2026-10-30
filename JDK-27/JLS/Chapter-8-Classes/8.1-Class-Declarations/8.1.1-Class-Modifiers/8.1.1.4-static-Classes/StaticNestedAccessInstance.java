/*
 * @test
 * @summary JLS 8.1.1.4: a static nested class has no immediately enclosing
 *          instance, so references from it to instance variables or instance
 *          methods of a lexically enclosing class are disallowed.
 * @compile/fail StaticNestedAccessInstance.java
 */

class Outer {
    int instanceVar = 1;

    static class StaticNestedAccessInstance {
        int bad() {
            return instanceVar;   // error: non-static variable cannot be referenced
        }
    }
}
