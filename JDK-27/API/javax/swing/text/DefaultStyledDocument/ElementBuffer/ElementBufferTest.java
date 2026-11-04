/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.ElementBuffer conformance: type shape via reflection (headless-safe).
 * @run main ElementBufferTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ElementBufferTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$ElementBuffer", false, ElementBufferTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRootElement"), "method getRootElement");
        check(names.contains("insert"), "method insert");
        check(names.contains("remove"), "method remove");
        check(names.contains("change"), "method change");
        check(names.contains("clone"), "method clone");

        // ElementBuffer is constructible around the default root and reports it back
        javax.swing.text.DefaultStyledDocument d = new javax.swing.text.DefaultStyledDocument();
        d.insertString(0, "one\ntwo", null);
        javax.swing.text.DefaultStyledDocument.ElementBuffer buf = d.new ElementBuffer(d.getDefaultRootElement());
        check(buf.getRootElement() == d.getDefaultRootElement(), "buffer wraps the root");
        check(buf.getClass().getName().equals("javax.swing.text.DefaultStyledDocument$ElementBuffer"),
                "buffer class identity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
