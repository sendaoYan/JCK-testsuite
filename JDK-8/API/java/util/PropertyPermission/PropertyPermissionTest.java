/*
 * @test
 * @summary java.util.PropertyPermission API conformance: name/actions and inheritance.
 * @run main PropertyPermissionTest
 */

import java.security.BasicPermission;
import java.util.PropertyPermission;

public class PropertyPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPropertyPermission() {
        PropertyPermission p = new PropertyPermission("java.home", "read");
        check(p instanceof BasicPermission, "extends BasicPermission");
        check("java.home".equals(p.getName()), "getName");
        check("read".equals(p.getActions()), "getActions");
    }


    // ===================== 契约深化：implies 矩阵/动作规范化/通配与前缀/权限集合 =====================
    static void testPermissionContracts() {
        PropertyPermission r = new PropertyPermission("my.key", "read");
        check(r.implies(new PropertyPermission("my.key", "read")), "same name+action implies");
        check(!r.implies(new PropertyPermission("my.key", "write")), "read does not imply write");
        check(!r.implies(new PropertyPermission("other.key", "read")), "different name not implied");
        check(new PropertyPermission("*", "read")
                    .implies(new PropertyPermission("anything.else", "read")),
              "wildcard * implies any name for read");
        PropertyPermission rw = new PropertyPermission("java.*", "read,write");
        check(rw.implies(new PropertyPermission("java.home", "read")),
              "prefix wildcard implies subkeys");
        check(rw.implies(new PropertyPermission("java.home", "write")),
              "prefix wildcard implies write on subkeys");
        check(!rw.implies(new PropertyPermission("javax.other", "read")),
              "prefix does not imply different prefix");
        check("read,write".equals(new PropertyPermission("k", "write,read").getActions()),
              "actions normalized to canonical order read,write");
        check(r.equals(new PropertyPermission("my.key", "read"))
              && r.hashCode() == new PropertyPermission("my.key", "read").hashCode(),
              "equals/hashCode keyed on name+actions");
        check(r.newPermissionCollection() != null, "newPermissionCollection");
        check(!r.implies(new java.io.FilePermission("x", "read")),
              "Permission implies only within same type");
    }

    public static void main(String[] args) {
        testPropertyPermission();
        testPermissionContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
