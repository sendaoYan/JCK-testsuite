/*
 * @test
 * @summary java.security.Permissions conformance: add, implies, elements.
 * @run main PermissionsTest
 */

import java.security.Permissions;
import java.security.AllPermission;
import java.security.SecurityPermission;

public class PermissionsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Permissions p = new Permissions();
        check(!p.implies(new SecurityPermission("a")), "empty implies false");
        p.add(new SecurityPermission("a"));
        check(p.implies(new SecurityPermission("a")), "implies added");
        check(!p.implies(new SecurityPermission("b")), "not implies absent");
        java.util.Enumeration<java.security.Permission> e = p.elements();
        check(e.hasMoreElements(), "elements has more");
        e.nextElement();
        check(!e.hasMoreElements(), "elements exhausted");
        p.add(new AllPermission());
        check(p.implies(new SecurityPermission("anything")), "all permission implies");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
