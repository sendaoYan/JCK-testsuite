/*
 * @test
 * @summary javax.security.auth.Destroyable conformance: white-box structure + black-box behavior.
 * @run main DestroyableTest
 */

import java.lang.reflect.*;
import javax.security.auth.Destroyable;
import javax.security.auth.DestroyFailedException;

public class DestroyableTest {
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
        Class<?> c = Class.forName("javax.security.auth.Destroyable");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"destroy","isDestroyed"});

        Method destroy = method(c, "destroy");
        Method isDestroyed = method(c, "isDestroyed");
        check(Modifier.isPublic(destroy.getModifiers()), "destroy public");
        check(destroy.isDefault(), "destroy is default method");
        check(destroy.getReturnType() == void.class, "destroy returns void");
        check(hasException(destroy, DestroyFailedException.class), "destroy throws DestroyFailedException");

        check(Modifier.isPublic(isDestroyed.getModifiers()), "isDestroyed public");
        check(isDestroyed.isDefault(), "isDestroyed is default method");
        check(isDestroyed.getReturnType() == boolean.class, "isDestroyed returns boolean");
    }

    static boolean hasException(Method m, Class<?> e) {
        for (Class<?> t : m.getExceptionTypes()) if (t == e) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // default implementation: not destroyed, destroy fails
        Destroyable dflt = new Destroyable() { };
        check(!dflt.isDestroyed(), "default isDestroyed false");
        boolean dfe = false;
        try { dflt.destroy(); } catch (DestroyFailedException e) { dfe = true; }
        check(dfe, "default destroy throws DestroyFailedException");

        // custom implementation exercises the full contract
        final boolean[] destroyed = { false };
        Destroyable custom = new Destroyable() {
            public void destroy() { destroyed[0] = true; }
            public boolean isDestroyed() { return destroyed[0]; }
        };
        check(!custom.isDestroyed(), "custom initially not destroyed");
        custom.destroy();
        check(custom.isDestroyed(), "custom destroyed after destroy");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
