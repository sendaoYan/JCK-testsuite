/*
 * @test
 * @summary JLS 6.5.7.1 (Java SE 8): an instance method may not be invoked by a
 *          simple method name from a static context.
 * @compile/fail InstanceMethodInStaticContext.java
 */

class InstanceMethodInStaticContext {
    void m() { }
    static void s() {
        m();   // error: non-static method m() cannot be referenced from a static context
    }
}
