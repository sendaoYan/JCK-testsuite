/*
 * @test
 * @summary java.security.PrivilegedExceptionAction conformance: white-box structure + black-box behavior.
 * @run main PrivilegedExceptionActionTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class PrivilegedExceptionActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.PrivilegedExceptionAction");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"run"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Lambda implementation that succeeds
        PrivilegedExceptionAction<String> ok = () -> "jck-ok";
        check("jck-ok".equals(ok.run()), "run");
        // checked exception from run() must be declared / handled at the call site
        PrivilegedExceptionAction<String> fail = () -> { throw new java.io.IOException("io-fail"); };
        try {
            fail.run();
            check(false, "run throws");
        } catch (java.io.IOException ioe) {
            check("io-fail".equals(ioe.getMessage()), "run throws checked");
        }
        // success path through doPrivileged
        check("jck-ok".equals(AccessController.doPrivileged(ok)), "doPrivileged ok");
        // checked exception path wraps into PrivilegedActionException
        try {
            AccessController.doPrivileged(fail);
            check(false, "wrapped");
        } catch (PrivilegedActionException pae) {
            check(pae.getCause() instanceof java.io.IOException, "cause type");
            check("io-fail".equals(pae.getCause().getMessage()), "cause message");
        }
        // runtime exception passes through unwrapped
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> { throw new IllegalStateException("rt"); });
            check(false, "rt propagates");
        } catch (IllegalStateException ise) {
            check("rt".equals(ise.getMessage()), "runtime unwrapped");
        }
        // with-context overload
        try {
            AccessController.doPrivileged(fail, AccessController.getContext());
            check(false, "wrapped ctx");
        } catch (PrivilegedActionException pae) {
            check(pae.getCause() instanceof java.io.IOException, "cause with ctx");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
