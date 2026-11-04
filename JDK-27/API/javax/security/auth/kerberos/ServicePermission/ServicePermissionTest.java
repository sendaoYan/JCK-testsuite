/*
 * @test
 * @summary javax.security.auth.kerberos.ServicePermission conformance: white-box structure + black-box behavior.
 * @run main ServicePermissionTest
 */

import java.security.Permission;
import java.io.Serializable;
import javax.security.auth.kerberos.ServicePermission;

@SuppressWarnings("removal")
public class ServicePermissionTest {
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
        Class<?> c = Class.forName("javax.security.auth.kerberos.ServicePermission");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Permission.class, "extends java.security.Permission");
        check(hasInterface(c, Serializable.class), "implements Serializable");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
        for (String m : new String[]{"implies", "equals", "hashCode", "getActions", "newPermissionCollection"}) {
            check(declares(c, m), "method " + m + " declared on class");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String name = "krbtgt/EXAMPLE.COM@EXAMPLE.COM";

        ServicePermission initiate = new ServicePermission(name, "initiate");
        check(name.equals(initiate.getName()), "getName");
        check("initiate".equals(initiate.getActions()), "getActions initiate");

        ServicePermission accept = new ServicePermission(name, "accept");
        check("accept".equals(accept.getActions()), "getActions accept");

        // canonical action ordering: initiate,accept regardless of input order
        ServicePermission both = new ServicePermission(name, "accept,initiate");
        check("initiate,accept".equals(both.getActions()), "getActions canonical ordering");
        ServicePermission both2 = new ServicePermission(name, "initiate,accept");
        check(both.equals(both2), "equals ignores action order");
        check(both.hashCode() == both2.hashCode(), "hashCode ignores action order");

        // implies: action mask subset + name match
        check(both.implies(initiate), "both implies initiate");
        check(both.implies(accept), "both implies accept");
        check(both.implies(both), "both implies self");
        check(initiate.implies(initiate), "initiate implies self");
        check(!initiate.implies(accept), "initiate not implies accept");
        check(!initiate.implies(both), "initiate not implies both");

        // wildcard name "*" implies any name
        ServicePermission wildcard = new ServicePermission("*", "initiate,accept");
        check(wildcard.implies(initiate), "wildcard name implies any initiate");
        check(wildcard.implies(new ServicePermission("other/service@REALM", "accept")), "wildcard name implies any accept");

        // "@REALM" suffix wildcard: this name endsWith p's realm-only name
        ServicePermission realmWild = new ServicePermission("@EXAMPLE.COM", "initiate");
        check(initiate.implies(realmWild), "service name implies its own @REALM");
        check(!initiate.implies(new ServicePermission("@OTHER.REALM", "initiate")), "service name not implies other @REALM");

        // implies non-ServicePermission -> false
        check(!initiate.implies(new javax.security.auth.kerberos.DelegationPermission("\"a\" \"b\"")),
                "not implies non-ServicePermission");

        // equals / hashCode: same name + same mask
        check(initiate.equals(new ServicePermission(name, "initiate")), "equals same name+action");
        check(initiate.hashCode() == new ServicePermission(name, "initiate").hashCode(), "hashCode same");
        check(!initiate.equals(accept), "not equals different action");
        check(!initiate.equals(new ServicePermission("other@REALM", "initiate")), "not equals different name");
        check(!initiate.equals(null), "not equals null");
        check(!initiate.equals("x"), "not equals non-permission");

        // action parsing edge: "-" -> no actions (empty canonical string)
        check("".equals(new ServicePermission(name, "-").getActions()), "dash action -> empty actions");

        // invalid arguments
        checkThrows(NullPointerException.class, () -> new ServicePermission(null, "initiate"), "null name -> NPE");
        checkThrows(NullPointerException.class, () -> new ServicePermission(name, null), "null action -> NPE");
        checkThrows(IllegalArgumentException.class, () -> new ServicePermission(name, ""), "empty action -> IAE");
        checkThrows(IllegalArgumentException.class, () -> new ServicePermission(name, "bogus"), "unknown action -> IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
