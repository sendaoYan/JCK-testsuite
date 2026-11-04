/*
 * @test
 * @summary JLS 6.5.6.1 (Java SE 8): a simple expression name that refers to an
 *          instance field may not be used in a static context.
 * @compile/fail InstanceFieldInStaticContext.java
 */

class InstanceFieldInStaticContext {
    int x = 1;
    static void m() {
        int y = x;   // error: non-static variable x cannot be referenced from a static context
    }
}
