/*
 * @test
 * @summary JLS 6.5.6.1 Example 6.5.6.1-2: an instance variable must not be
 *          referenced from a static context.
 * @compile/fail InstanceVariableInStaticContext.java
 */

class InstanceVariableInStaticContext {
    static String a;
    String b;
    static String concat() {
        return a + b;   // error: non-static field b referenced from a static context
    }
}
