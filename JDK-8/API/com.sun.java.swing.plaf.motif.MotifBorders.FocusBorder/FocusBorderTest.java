/*
 * @test
 * @summary com.sun.java.swing.plaf.motif.MotifBorders.FocusBorder conformance: type shape via reflection (headless-safe).
 * @run main FocusBorderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FocusBorderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.motif.MotifBorders$FocusBorder", false, FocusBorderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.motif.MotifBorders$FocusBorder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.AbstractBorder"), "superclass");
        check(hasInterface(c, "javax.swing.plaf.UIResource"), "implements javax.swing.plaf.UIResource");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("paintBorder"), "method paintBorder");
        check(names.contains("getBorderInsets"), "method getBorderInsets");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
