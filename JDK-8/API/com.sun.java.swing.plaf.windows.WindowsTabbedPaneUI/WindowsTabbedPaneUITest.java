/*
 * @test
 * @summary com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI conformance: type shape via reflection (headless-safe).
 * @run main WindowsTabbedPaneUITest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class WindowsTabbedPaneUITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI", false, WindowsTabbedPaneUITest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicTabbedPaneUI"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("installDefaults"), "method installDefaults");
        check(names.contains("uninstallDefaults"), "method uninstallDefaults");
        check(names.contains("createUI"), "method createUI");
        check(names.contains("setRolloverTab"), "method setRolloverTab");
        check(names.contains("paintContentBorder"), "method paintContentBorder");
        check(names.contains("paintTabBackground"), "method paintTabBackground");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
