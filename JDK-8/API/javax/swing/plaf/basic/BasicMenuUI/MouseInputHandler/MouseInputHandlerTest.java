/*
 * @test
 * @summary javax.swing.plaf.basic.BasicMenuUI.MouseInputHandler conformance: type shape via reflection (headless-safe).
 * @run main MouseInputHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MouseInputHandlerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicMenuUI$MouseInputHandler", false, MouseInputHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.event.MouseInputListener"), "implements javax.swing.event.MouseInputListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("mouseClicked"), "method mouseClicked");
        check(names.contains("mousePressed"), "method mousePressed");
        check(names.contains("mouseReleased"), "method mouseReleased");
        check(names.contains("mouseEntered"), "method mouseEntered");
        check(names.contains("mouseExited"), "method mouseExited");
        check(names.contains("mouseDragged"), "method mouseDragged");
        check(names.contains("mouseMoved"), "method mouseMoved");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
