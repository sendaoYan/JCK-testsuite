/*
 * @test
 * @summary javax.smartcardio.CardPermission conformance: white-box structure + black-box behavior.
 * @run main CardPermissionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Permission;

public class CardPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void assertThrows(Class<? extends Throwable> t, Runnable r, String m) {
        try { r.run(); } catch (Throwable x) {
            check(t.isInstance(x), m + " (threw " + x.getClass().getSimpleName() + ")");
            return;
        }
        check(false, m + " (no throw)");
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.CardPermission");
        check(!c.isInterface(), "CardPermission not interface");
        check(Permission.class.isAssignableFrom(c), "CardPermission extends Permission");
        check(c.getConstructor(String.class, String.class) != null, "CardPermission(String,String) ctor");
        for (String n : new String[]{"getActions", "implies", "equals", "hashCode"}) {
            check(hasMethod(c, n), "CardPermission declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.smartcardio.CardPermission p = new javax.smartcardio.CardPermission("*", "connect");
        check("*".equals(p.getName()), "CardPermission getName");
        check("connect".equals(p.getActions()), "CardPermission getActions");

        // canonical action strings (from JDK TestCardPermission)
        check("connect,reset".equals(new javax.smartcardio.CardPermission("*", "Reset,coNnect").getActions()),
                "CardPermission canonical order/whitespace-less");
        check("*".equals(new javax.smartcardio.CardPermission("*", "exclusive,*,connect").getActions()),
                "CardPermission '*' action collapses to '*'");
        check("*".equals(new javax.smartcardio.CardPermission("*", "connect,reset,exclusive,transmitControl,getBasicChannel,openLogicalChannel").getActions()),
                "CardPermission all actions collapses to '*'");

        // null actions -> canonical null
        check(new javax.smartcardio.CardPermission("*", null).getActions() == null, "CardPermission null actions");

        // implies: same action
        check(p.implies(new javax.smartcardio.CardPermission("*", "connect")), "implies connect");
        check(!p.implies(new javax.smartcardio.CardPermission("*", "reset")), "not implies reset");

        // implies: name is '*' covers any terminal name
        javax.smartcardio.CardPermission all = new javax.smartcardio.CardPermission("*", "connect,reset");
        check(all.implies(new javax.smartcardio.CardPermission("AnyTerminal", "reset")), "implies '*' name covers any");
        check(all.implies(new javax.smartcardio.CardPermission("AnyTerminal", "connect")), "implies '*' name covers connect");

        // implies: subset of actions
        javax.smartcardio.CardPermission p1 = new javax.smartcardio.CardPermission("*", "connect,reset");
        check(!p1.implies(new javax.smartcardio.CardPermission("*", "transmitControl")), "not implies non-subset action");
        check(p1.implies(new javax.smartcardio.CardPermission("*", "connect")), "implies subset action");

        // implies: same name (not '*') and subset
        javax.smartcardio.CardPermission t1 = new javax.smartcardio.CardPermission("Term1", "connect,reset");
        check(t1.implies(new javax.smartcardio.CardPermission("Term1", "reset")), "implies same name subset");
        check(!t1.implies(new javax.smartcardio.CardPermission("Term2", "reset")), "not implies different name");

        // implies: non-CardPermission -> false
        Permission foreign = new Permission("connect") {
            public boolean implies(Permission permission) { return true; }
            public boolean equals(Object obj) { return false; }
            public int hashCode() { return 0; }
            public String getActions() { return "connect"; }
        };
        check(!p.implies(foreign), "not implies non-CardPermission");

        // equals / hashCode
        javax.smartcardio.CardPermission p2 = new javax.smartcardio.CardPermission("*", "connect");
        check(p.equals(p2) && p2.equals(p), "CardPermission equals");
        check(p.hashCode() == p2.hashCode(), "CardPermission hashCode");
        check(!p.equals(new javax.smartcardio.CardPermission("*", "reset")), "not equals different actions");
        check(!p.equals(new javax.smartcardio.CardPermission("Term", "connect")), "not equals different name");
        check(!p.equals(null) && !p.equals("x"), "not equals non-CardPermission");

        // exceptions
        assertThrows(NullPointerException.class, () -> new javax.smartcardio.CardPermission(null, "connect"), "null name NPE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CardPermission("*", ""), "empty actions IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CardPermission("*", "foo"), "invalid action IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CardPermission("*", "connect,"), "trailing comma IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CardPermission("*", ",connect"), "leading comma IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CardPermission("*", "connect,,reset"), "double comma IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CardPermission("*", "connect, reset"), "space after comma IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
