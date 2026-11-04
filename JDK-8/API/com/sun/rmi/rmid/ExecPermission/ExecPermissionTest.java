/*
 * @test
 * @summary com.sun.rmi.rmid.ExecPermission conformance: name/implies/equals semantics.
 * @run main ExecPermissionTest
 */

import com.sun.rmi.rmid.ExecPermission;
import java.security.Permission;

public class ExecPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        ExecPermission p = new ExecPermission("/usr/bin/java");
        check(p instanceof Permission, "extends Permission");
        check("/usr/bin/java".equals(p.getName()), "getName");
        check(p.getActions() != null, "getActions non-null");
        check(p.implies(new ExecPermission("/usr/bin/java")), "implies self");
        check(!p.implies(new ExecPermission("/other")), "not implies other");
        check(p.equals(new ExecPermission("/usr/bin/java", "read")), "equals same name");
        check(!p.equals(new ExecPermission("/other")), "not equals other");
        check(p.hashCode() == new ExecPermission("/usr/bin/java", "read").hashCode(), "hashCode same name");
        check(p.newPermissionCollection() != null, "newPermissionCollection");

        ExecPermission p2 = new ExecPermission("/usr/bin/java", "read");
        check("/usr/bin/java".equals(p2.getName()), "two-arg getName");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
