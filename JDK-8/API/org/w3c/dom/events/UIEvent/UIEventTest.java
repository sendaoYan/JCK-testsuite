/*
 * @test
 * @summary org.w3c.dom.events.UIEvent conformance: reflection shape checks.
 * @run main UIEventTest
 */

import java.lang.reflect.*;

public class UIEventTest {
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
        Class<?> c = Class.forName("org.w3c.dom.events.UIEvent");
        check(c != null, "org.w3c.dom.events.UIEvent loads");
        check(c.getName().equals("org.w3c.dom.events.UIEvent"), "binary name is org.w3c.dom.events.UIEvent");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.events.UIEvent is public");
        check(c.isInterface(), "org.w3c.dom.events.UIEvent is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.events.Event")), "direct superinterfaces");
        check(declares(c, "getView"), "declares getView");
        check(declares(c, "getDetail"), "declares getDetail");
        check(declares(c, "initUIEvent"), "declares initUIEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
