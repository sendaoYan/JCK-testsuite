/*
 * @test
 * @summary com.sun.java.swing.plaf.windows.WindowsScrollBarUI conformance: type shape via reflection (headless-safe).
 * @run main WindowsScrollBarUITest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class WindowsScrollBarUITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.windows.WindowsScrollBarUI", false, WindowsScrollBarUITest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.windows.WindowsScrollBarUI"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicScrollBarUI"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("createUI"), "method createUI");
        check(names.contains("installDefaults"), "method installDefaults");
        check(names.contains("getMinimumThumbSize"), "method getMinimumThumbSize");
        check(names.contains("uninstallUI"), "method uninstallUI");
        check(names.contains("configureScrollBarColors"), "method configureScrollBarColors");
        check(names.contains("createDecreaseButton"), "method createDecreaseButton");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
