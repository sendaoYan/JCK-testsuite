/*
 * @test
 * @summary javax.swing.border.AbstractBorder conformance: type shape via reflection (headless-safe).
 * @run main AbstractBorderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.border.AbstractBorder", false, AbstractBorderTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.border.Border"), "implements javax.swing.border.Border");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("paintBorder"), "method paintBorder");
        check(names.contains("getBorderInsets"), "method getBorderInsets");
        check(names.contains("getBorderInsets"), "method getBorderInsets");
        check(names.contains("isBorderOpaque"), "method isBorderOpaque");
        check(names.contains("getInteriorRectangle"), "method getInteriorRectangle");
        check(names.contains("getInteriorRectangle"), "method getInteriorRectangle");
        check(names.contains("getBaseline"), "method getBaseline");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
