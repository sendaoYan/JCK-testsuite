/*
 * @test
 * @summary com.sun.rmi.rmid.ExecOptionPermission conformance: name/implies/equals semantics.
 * @run main ExecOptionPermissionTest
 */

import com.sun.rmi.rmid.ExecOptionPermission;
import java.security.Permission;

public class ExecOptionPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        ExecOptionPermission o = new ExecOptionPermission("-verbose");
        check(o instanceof Permission, "extends Permission");
        check("-verbose".equals(o.getName()), "getName");
        check(o.getActions() != null, "getActions non-null");
        check(o.implies(new ExecOptionPermission("-verbose")), "implies self");
        check(!o.implies(new ExecOptionPermission("-quiet")), "not implies other");
        check(o.equals(new ExecOptionPermission("-verbose")), "equals");
        check(o.hashCode() == new ExecOptionPermission("-verbose").hashCode(), "hashCode");
        check(o.newPermissionCollection() != null, "newPermissionCollection");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
