/*
 * @test
 * @summary javax.security.auth.kerberos.DelegationPermission conformance: white-box structure + black-box behavior.
 * @run main DelegationPermissionTest
 */

import java.security.BasicPermission;
import java.io.Serializable;
import javax.security.auth.kerberos.DelegationPermission;

@SuppressWarnings("removal")
public class DelegationPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " [expected " + type.getSimpleName() + ", got " + t + "]", t);
        }
        throw new AssertionError(msg + " [expected " + type.getSimpleName() + "]");
    }
    static boolean hasInterface(Class<?> c, Class<?> want) {
        for (Class<?> i : c.getInterfaces()) if (i == want) return true;
        return false;
    }
    static boolean declares(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.kerberos.DelegationPermission");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == BasicPermission.class, "extends java.security.BasicPermission");
        check(hasInterface(c, Serializable.class), "implements Serializable");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
        for (String m : new String[]{"implies", "equals", "hashCode", "newPermissionCollection"}) {
            check(declares(c, m), "method " + m + " declared on class");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String name = "\"backup/bar.example.com@EXAMPLE.COM\" \"krbtgt/EXAMPLE.COM@EXAMPLE.COM\"";
        DelegationPermission dp = new DelegationPermission(name);
        check(name.equals(dp.getName()), "getName returns raw target");
        check("".equals(dp.getActions()), "getActions empty for single-arg ctor");

        DelegationPermission dp2 = new DelegationPermission(name, null);
        check("".equals(dp2.getActions()), "getActions empty for null actions");

        // implies is exactly equals (no wildcard semantics)
        check(dp.implies(dp), "implies self");
        check(dp.implies(new DelegationPermission(name)), "implies equal permission");
        check(!dp.implies(new DelegationPermission("\"backup/other@EXAMPLE.COM\" \"krbtgt/EXAMPLE.COM@EXAMPLE.COM\"")),
                "implies false for different subordinate");

        // equality / hashCode: subordinate + service, whitespace-insensitive
        String spaced = "\"backup/bar.example.com@EXAMPLE.COM\"   \"krbtgt/EXAMPLE.COM@EXAMPLE.COM\"";
        DelegationPermission spacedDp = new DelegationPermission(spaced);
        check(dp.equals(spacedDp), "equals ignores inter-principal whitespace");
        check(spacedDp.equals(dp), "equals symmetric whitespace");
        check(dp.hashCode() == spacedDp.hashCode(), "hashCode equal for whitespace variants");
        check(dp.equals(dp), "equals self");
        check(!dp.equals(null), "not equals null");
        check(!dp.equals("x"), "not equals non-permission");
        check(!dp.equals(new DelegationPermission("\"krbtgt/EXAMPLE.COM@EXAMPLE.COM\" \"backup/bar.example.com@EXAMPLE.COM\"")),
                "not equals swapped subordinate/service");

        // bad input -> IllegalArgumentException
        checkThrows(IllegalArgumentException.class, () -> new DelegationPermission("\"aaa\""), "single principal -> IAE");
        checkThrows(IllegalArgumentException.class, () -> new DelegationPermission("aaa"), "unquoted -> IAE");
        checkThrows(IllegalArgumentException.class, () -> new DelegationPermission("\"aaa\"-\"bbb\""), "improperly separated -> IAE");
        checkThrows(NullPointerException.class, () -> new DelegationPermission(null), "null target -> NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
