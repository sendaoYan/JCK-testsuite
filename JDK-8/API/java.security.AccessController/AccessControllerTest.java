/*
 * @test
 * @summary java.security.AccessController conformance: getContext and doPrivileged.
 * @run main AccessControllerTest
 */

public class AccessControllerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.AccessControlContext ctx = java.security.AccessController.getContext();
        check(ctx != null, "getContext non-null");
        String result = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
            public String run() { return "ok"; }
        });
        check("ok".equals(result), "doPrivileged returns value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
