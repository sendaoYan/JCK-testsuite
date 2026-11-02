/*
 * @test
 * @summary javax.swing.text.html.parser.DTD conformance: type shape via reflection (headless-safe).
 * @run main DTDTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DTDTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.DTD", false, DTDTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.text.html.parser.DTDConstants"), "implements javax.swing.text.html.parser.DTDConstants");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("getEntity"), "method getEntity");
        check(names.contains("getEntity"), "method getEntity");
        check(names.contains("getElement"), "method getElement");
        check(names.contains("getElement"), "method getElement");
        check(names.contains("defineEntity"), "method defineEntity");
        check(names.contains("defineElement"), "method defineElement");
        check(names.contains("defineAttributes"), "method defineAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
