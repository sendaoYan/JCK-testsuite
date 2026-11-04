/*
 * @test
 * @summary javax.swing.JEditorPane.JEditorPaneAccessibleHypertextSupport conformance: type shape via reflection (headless-safe).
 * @run main JEditorPaneAccessibleHypertextSupportTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JEditorPaneAccessibleHypertextSupportTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JEditorPane$JEditorPaneAccessibleHypertextSupport", false, JEditorPaneAccessibleHypertextSupportTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JEditorPane$AccessibleJEditorPane"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleHypertext"), "implements javax.accessibility.AccessibleHypertext");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLinkCount"), "method getLinkCount");
        check(names.contains("getLinkIndex"), "method getLinkIndex");
        check(names.contains("getLink"), "method getLink");
        check(names.contains("getLinkText"), "method getLinkText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
