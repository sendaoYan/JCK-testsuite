/*
 * @test
 * @summary JLS 8.1.3: instance variables of an enclosing class are not available
 *          inside a local class declared in a static context.
 * @compile/fail LocalClassInStaticContext.java
 */

class Outer {
    int i = 100;

    static void classMethod() {
        final int l = 200;
        class LocalClassInStaticContext {
            int k = i;   // error: non-static variable i
            int m = l;   // OK
        }
    }
}
