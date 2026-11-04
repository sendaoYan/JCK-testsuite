/*
 * @test
 * @summary javax.smartcardio.TerminalFactorySpi conformance: reflection shape checks.
 * @run main TerminalFactorySpiTest
 */

import java.lang.reflect.*;

public class TerminalFactorySpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.TerminalFactorySpi");
        check(c != null, "javax.smartcardio.TerminalFactorySpi loads");
        check(c.getName().equals("javax.smartcardio.TerminalFactorySpi"), "binary name is javax.smartcardio.TerminalFactorySpi");
        check(Modifier.isPublic(c.getModifiers()), "javax.smartcardio.TerminalFactorySpi is public");
        check(!c.isInterface(), "javax.smartcardio.TerminalFactorySpi is not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "javax.smartcardio.TerminalFactorySpi is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
