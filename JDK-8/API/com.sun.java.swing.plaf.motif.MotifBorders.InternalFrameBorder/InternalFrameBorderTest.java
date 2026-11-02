/*
 * @test
 * @summary com.sun.java.swing.plaf.motif.MotifBorders.InternalFrameBorder conformance: type shape via reflection (headless-safe).
 * @run main InternalFrameBorderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class InternalFrameBorderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.motif.MotifBorders$InternalFrameBorder", false, InternalFrameBorderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.motif.MotifBorders$InternalFrameBorder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.java.swing.plaf.motif.MotifBorders$FrameBorder"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("setFrame"), "method setFrame");
        check(names.contains("frame"), "method frame");
        check(names.contains("resizePartWidth"), "method resizePartWidth");
        check(names.contains("drawTopBorder"), "method drawTopBorder");
        check(names.contains("drawLeftBorder"), "method drawLeftBorder");
        check(names.contains("drawRightBorder"), "method drawRightBorder");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
