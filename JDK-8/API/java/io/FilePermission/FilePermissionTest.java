/*
 * @test
 * @summary java.io.FilePermission API conformance: name/actions/implies.
 * @run main FilePermissionTest
 */

import java.io.FilePermission;
import java.security.Permission;

public class FilePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFilePermission() {
        FilePermission p = new FilePermission("/tmp/-", "read,write");
        check(p instanceof Permission, "extends Permission");
        check("/tmp/-".equals(p.getName()), "getName");
        check("read,write".equals(p.getActions()), "getActions");
        FilePermission r = new FilePermission("/tmp/-", "read");
        check(p.implies(r), "implies read subset");
        check(!r.implies(p), "does not imply write superset");
    }

    public static void main(String[] args) {
        testFilePermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
