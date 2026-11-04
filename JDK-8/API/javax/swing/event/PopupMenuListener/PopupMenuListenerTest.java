/*
 * @test
 * @summary javax.swing.event.PopupMenuListener conformance: type shape via reflection (headless-safe).
 * @run main PopupMenuListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PopupMenuListenerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.event.PopupMenuListener", false, PopupMenuListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("popupMenuWillBecomeVisible"), "method popupMenuWillBecomeVisible");
        check(names.contains("popupMenuWillBecomeInvisible"), "method popupMenuWillBecomeInvisible");
        check(names.contains("popupMenuCanceled"), "method popupMenuCanceled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
