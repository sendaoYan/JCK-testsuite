/*
 * @test
 * @summary java.security.AccessControlException conformance: constructors and getPermission.
 * @run main AccessControlExceptionTest
 */

import java.security.AccessControlException;
import java.security.AllPermission;

public class AccessControlExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AccessControlException e1 = new AccessControlException("msg");
        check("msg".equals(e1.getMessage()), "message from (String)");
        check(e1.getPermission() == null, "getPermission null");
        AllPermission perm = new AllPermission();
        AccessControlException e2 = new AccessControlException("msg2", perm);
        check(perm.equals(e2.getPermission()), "getPermission");
        check(e1 instanceof SecurityException, "extends SecurityException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
