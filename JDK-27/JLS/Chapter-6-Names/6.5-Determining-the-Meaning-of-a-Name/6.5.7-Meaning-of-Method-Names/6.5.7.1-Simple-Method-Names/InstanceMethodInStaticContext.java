/*
 * @test
 * @summary JLS 6.5.7.1: a reference to an instance method from a static context
 *          is a compile-time error.
 * @compile/fail InstanceMethodInStaticContext.java
 */

class InstanceMethodInStaticContext {
    void instanceMethod() {}
    static void m() {
        instanceMethod();   // error: non-static method in a static context
    }
}
