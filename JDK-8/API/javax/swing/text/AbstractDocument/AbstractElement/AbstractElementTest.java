/*
 * @test
 * @summary javax.swing.text.AbstractDocument.AbstractElement conformance: type shape via reflection (headless-safe).
 * @run main AbstractElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$AbstractElement", false, AbstractElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.text.Element"), "implements javax.swing.text.Element");
        check(hasInterface(c, "javax.swing.text.MutableAttributeSet"), "implements javax.swing.text.MutableAttributeSet");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasInterface(c, "javax.swing.tree.TreeNode"), "implements javax.swing.tree.TreeNode");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("dump"), "method dump");
        check(names.contains("getAttributeCount"), "method getAttributeCount");
        check(names.contains("isDefined"), "method isDefined");
        check(names.contains("isEqual"), "method isEqual");
        check(names.contains("copyAttributes"), "method copyAttributes");
        check(names.contains("getAttribute"), "method getAttribute");
        check(names.contains("getAttributeNames"), "method getAttributeNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
