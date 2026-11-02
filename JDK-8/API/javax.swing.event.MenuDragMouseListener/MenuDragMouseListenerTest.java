/*
 * @test
 * @summary javax.swing.event.MenuDragMouseListener conformance: type shape via reflection (headless-safe).
 * @run main MenuDragMouseListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MenuDragMouseListenerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuDragMouseListener", false, MenuDragMouseListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("menuDragMouseEntered"), "method menuDragMouseEntered");
        check(names.contains("menuDragMouseExited"), "method menuDragMouseExited");
        check(names.contains("menuDragMouseDragged"), "method menuDragMouseDragged");
        check(names.contains("menuDragMouseReleased"), "method menuDragMouseReleased");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
