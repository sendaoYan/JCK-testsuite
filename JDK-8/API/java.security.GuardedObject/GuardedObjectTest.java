/*
 * @test
 * @summary java.security.GuardedObject conformance: construct with guard and getObject.
 * @run main GuardedObjectTest
 */

public class GuardedObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.Guard g = new java.security.Guard() { public void checkGuard(Object o) {} };
        java.security.GuardedObject go = new java.security.GuardedObject("x", g);
        check("x".equals(go.getObject()), "getObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
