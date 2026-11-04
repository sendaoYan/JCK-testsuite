/*
 * @test
 * @summary com.sun.java.swing.plaf.motif.MotifOptionPaneUI conformance: type shape via reflection (headless-safe).
 * @run main MotifOptionPaneUITest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MotifOptionPaneUITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.motif.MotifOptionPaneUI", false, MotifOptionPaneUITest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.motif.MotifOptionPaneUI"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicOptionPaneUI"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("createUI"), "method createUI");
        check(names.contains("createButtonArea"), "method createButtonArea");
        check(names.contains("getMinimumOptionPaneSize"), "method getMinimumOptionPaneSize");
        check(names.contains("createSeparator"), "method createSeparator");
        check(names.contains("addIcon"), "method addIcon");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
