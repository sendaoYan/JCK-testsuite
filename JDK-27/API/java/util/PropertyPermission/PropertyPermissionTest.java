/*
 * @test
 * @summary java.util.PropertyPermission conformance: white-box structure + black-box behavior.
 * @run main PropertyPermissionTest
 */

import java.security.Permission;
import java.util.PropertyPermission;

@SuppressWarnings("removal")
public class PropertyPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.PropertyPermission");
        check(Permission.class.isAssignableFrom(c), "extends Permission");
        c.getConstructor(String.class, String.class);
        check(c.getDeclaredMethod("implies", Permission.class).getReturnType() == boolean.class,
                "implies boolean");
    }

    static void testBlackBox() {
        PropertyPermission p = new PropertyPermission("java.home", "write,read");
        check("read,write".equals(p.getActions()), "actions normalized/sorted");

        check(new PropertyPermission("java.*", "read").implies(
                new PropertyPermission("java.home", "read")), "wildcard implies specific");
        check(!new PropertyPermission("java.home", "read").implies(
                new PropertyPermission("java.home", "write")), "read does not imply write");

        expectThrows(() -> new PropertyPermission("x", "exec"), IllegalArgumentException.class,
                "invalid action");

        PropertyPermission a = new PropertyPermission("java.home", "read");
        PropertyPermission b = new PropertyPermission("java.home", "read");
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");
        check(p.newPermissionCollection() != null, "newPermissionCollection");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPermissionContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
