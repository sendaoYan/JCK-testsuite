/*
 * @test
 * @summary javax.swing.JInternalFrame.JDesktopIcon conformance: type shape via reflection (headless-safe).
 * @run main JDesktopIconTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JDesktopIconTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JInternalFrame$JDesktopIcon", false, JDesktopIconTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("getInternalFrame"), "method getInternalFrame");
        check(names.contains("setInternalFrame"), "method setInternalFrame");
        check(names.contains("getDesktopPane"), "method getDesktopPane");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
