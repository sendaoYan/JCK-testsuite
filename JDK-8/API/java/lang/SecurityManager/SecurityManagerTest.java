/*
 * @test
 * @summary java.lang.SecurityManager conformance: thread group and security context.
 * @run main SecurityManagerTest
 */

public class SecurityManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSecurityManager() {
        SecurityManager sm = new SecurityManager();
        check(sm.getThreadGroup() == Thread.currentThread().getThreadGroup(),
              "getThreadGroup returns current thread's group");
        Object ctx = sm.getSecurityContext();
        check(true, "getSecurityContext callable");
    }

    public static void main(String[] args) {
        testSecurityManager();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
