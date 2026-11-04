/*
 * @test
 * @summary javax.security.auth.Refreshable conformance: white-box structure + black-box behavior.
 * @run main RefreshableTest
 */

import java.lang.reflect.*;
import javax.security.auth.Refreshable;
import javax.security.auth.RefreshFailedException;

public class RefreshableTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static Method method(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return m;
        return null;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.Refreshable");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"isCurrent","refresh"});

        Method isCurrent = method(c, "isCurrent");
        Method refresh = method(c, "refresh");
        check(isCurrent.getReturnType() == boolean.class, "isCurrent returns boolean");
        check(Modifier.isAbstract(isCurrent.getModifiers()), "isCurrent abstract");
        check(refresh.getReturnType() == void.class, "refresh returns void");
        check(Modifier.isAbstract(refresh.getModifiers()), "refresh abstract");
        check(hasException(refresh, RefreshFailedException.class), "refresh throws RefreshFailedException");
    }

    static boolean hasException(Method m, Class<?> e) {
        for (Class<?> t : m.getExceptionTypes()) if (t == e) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final boolean[] current = { false };
        final boolean[] refreshed = { false };
        Refreshable r = new Refreshable() {
            public boolean isCurrent() { return current[0]; }
            public void refresh() { refreshed[0] = true; current[0] = true; }
        };

        check(!r.isCurrent(), "initially not current");
        r.refresh();
        check(refreshed[0], "refresh invoked");
        check(r.isCurrent(), "current after refresh");

        // refresh may throw checked RefreshFailedException
        Refreshable failing = new Refreshable() {
            public boolean isCurrent() { return false; }
            public void refresh() throws RefreshFailedException { throw new RefreshFailedException("expired"); }
        };
        boolean rfe = false;
        try { failing.refresh(); } catch (RefreshFailedException e) { rfe = true; }
        check(rfe, "refresh propagates RefreshFailedException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
