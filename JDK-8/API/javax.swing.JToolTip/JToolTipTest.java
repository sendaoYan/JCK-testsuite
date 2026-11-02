/*
 * @test
 * @summary javax.swing.JToolTip conformance: type shape via reflection (headless-safe).
 * @run main JToolTipTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JToolTipTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JToolTip", false, JToolTipTest.class.getClassLoader());
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
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("setTipText"), "method setTipText");
        check(names.contains("getTipText"), "method getTipText");
        check(names.contains("setComponent"), "method setComponent");
        check(names.contains("getComponent"), "method getComponent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
