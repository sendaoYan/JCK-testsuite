/*
 * @test
 * @summary com.sun.java.swing.plaf.windows.DesktopProperty conformance: type shape via reflection (headless-safe).
 * @run main DesktopPropertyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DesktopPropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.windows.DesktopProperty", false, DesktopPropertyTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.windows.DesktopProperty"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.UIDefaults$ActiveValue"), "implements javax.swing.UIDefaults$ActiveValue");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("createValue"), "method createValue");
        check(names.contains("getValueFromDesktop"), "method getValueFromDesktop");
        check(names.contains("getDefaultValue"), "method getDefaultValue");
        check(names.contains("invalidate"), "method invalidate");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("configureValue"), "method configureValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
