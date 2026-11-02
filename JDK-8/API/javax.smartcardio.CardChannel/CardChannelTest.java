/*
 * @test
 * @summary javax.smartcardio.CardChannel conformance: reflection shape checks.
 * @run main CardChannelTest
 */

import java.lang.reflect.*;

public class CardChannelTest {
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
        Class<?> c = Class.forName("javax.smartcardio.CardChannel");
        check(c != null, "javax.smartcardio.CardChannel loads");
        check(c.getName().equals("javax.smartcardio.CardChannel"), "binary name is javax.smartcardio.CardChannel");
        check(Modifier.isPublic(c.getModifiers()), "javax.smartcardio.CardChannel is public");
        check(!c.isInterface(), "javax.smartcardio.CardChannel is not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "javax.smartcardio.CardChannel is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
