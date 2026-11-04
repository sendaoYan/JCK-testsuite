/*
 * @test
 * @summary javax.swing.event.MenuListener conformance: white-box structure + black-box behavior.
 * @run main MenuListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.HashSet;
import java.util.Set;

public class MenuListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getMethod(name, params);
            check(m.getReturnType() == void.class, name + " returns void");
            check(Modifier.isAbstract(m.getModifiers()), name + " abstract");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing method " + name);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "interface 0");
        checkMethod(c, "menuSelected", MenuEvent.class);
        checkMethod(c, "menuDeselected", MenuEvent.class);
        checkMethod(c, "menuCanceled", MenuEvent.class);
        check(c.getDeclaredMethods().length == 3, "declared method count == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final MenuEvent[] last = new MenuEvent[1];
        MenuListener l = new MenuListener() {
            public void menuSelected(MenuEvent e) { fired[0] = "selected"; last[0] = e; }
            public void menuDeselected(MenuEvent e) { fired[0] = "deselected"; last[0] = e; }
            public void menuCanceled(MenuEvent e) { fired[0] = "canceled"; last[0] = e; }
        };

        Object src = new Object();
        MenuEvent selected = new MenuEvent(src);
        l.menuSelected(selected);
        check("selected".equals(fired[0]) && last[0] == selected, "menuSelected fired");
        check(last[0].getSource() == src, "menuSelected source round-trip");

        MenuEvent deselected = new MenuEvent(src);
        l.menuDeselected(deselected);
        check("deselected".equals(fired[0]) && last[0] == deselected, "menuDeselected fired");

        MenuEvent canceled = new MenuEvent(src);
        l.menuCanceled(canceled);
        check("canceled".equals(fired[0]) && last[0] == canceled, "menuCanceled fired");
        check(last[0].getSource() == src, "menuCanceled source round-trip");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuListener", false, MenuListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("menuSelected"), "method menuSelected");
        check(names.contains("menuDeselected"), "method menuDeselected");
        check(names.contains("menuCanceled"), "method menuCanceled");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
