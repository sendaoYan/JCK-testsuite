/*
 * @test
 * @summary java.security.AccessController conformance: doPrivileged and getContext.
 * @run main AccessControllerTest
 */

import java.security.AccessController;
import java.security.PrivilegedAction;

public class AccessControllerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        String r = AccessController.doPrivileged((PrivilegedAction<String>) () -> "result");
        check("result".equals(r), "doPrivileged returns value");
        check(AccessController.getContext() != null, "getContext non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
