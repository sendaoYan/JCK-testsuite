/*
 * @test
 * @summary javax.swing.text.html.parser.Element conformance: type shape via reflection (headless-safe).
 * @run main ElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.Element", false, ElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.text.html.parser.DTDConstants"), "implements javax.swing.text.html.parser.DTDConstants");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("omitStart"), "method omitStart");
        check(names.contains("omitEnd"), "method omitEnd");
        check(names.contains("getType"), "method getType");
        check(names.contains("getContent"), "method getContent");
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("getIndex"), "method getIndex");
        check(names.contains("isEmpty"), "method isEmpty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
