/*
 * @test
 * @summary JLS 6.5.7.1 / 15.12.2.5 (Java SE 8): an invocation is a compile-time
 *          error if no applicable method is the most specific (e.g. two
 *          overloads that are equally specific for the argument type).
 * @compile/fail AmbiguousMethod.java
 */

class AmbiguousMethod {
    static void f(String s, int i) { }
    static void f(int i, String s) { }

    static void test() {
        f("x", "y");   // error: reference to f is ambiguous
    }
}
