/*
 * @test
 * @summary javax.swing.plaf.basic.BasicToolBarUI.DragWindow conformance: type shape via reflection (headless-safe).
 * @run main DragWindowTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DragWindowTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicToolBarUI$DragWindow", false, DragWindowTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Window"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOrientation"), "method getOrientation");
        check(names.contains("setOrientation"), "method setOrientation");
        check(names.contains("getOffset"), "method getOffset");
        check(names.contains("setOffset"), "method setOffset");
        check(names.contains("setBorderColor"), "method setBorderColor");
        check(names.contains("getBorderColor"), "method getBorderColor");
        check(names.contains("paint"), "method paint");
        check(names.contains("getInsets"), "method getInsets");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
