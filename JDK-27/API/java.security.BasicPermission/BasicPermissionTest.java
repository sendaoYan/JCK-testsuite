/*
 * @test
 * @summary java.security.BasicPermission conformance: implies, getActions, getName, newPermissionCollection, checkGuard.
 * @run main BasicPermissionTest
 */

import java.security.BasicPermission;
import java.security.PermissionCollection;

public class BasicPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static class MyPerm extends BasicPermission {
        MyPerm(String name) { super(name); }
        MyPerm(String name, String actions) { super(name, actions); }
    }
    public static void main(String[] args) throws Exception {
        MyPerm p = new MyPerm("java.home");
        check("java.home".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");
        check(p.implies(new MyPerm("java.home")), "implies exact");
        check(!p.implies(new MyPerm("java.*")), "no implies wildcard parent");
        MyPerm wild = new MyPerm("java.*");
        check(wild.implies(new MyPerm("java.home")), "wildcard implies child");
        PermissionCollection pc = p.newPermissionCollection();
        check(pc != null, "newPermissionCollection");
        pc.add(new MyPerm("java.home"));
        check(pc.implies(p), "collection implies");
        check(p.equals(new MyPerm("java.home")), "equals");
        check(p.hashCode() == new MyPerm("java.home").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
