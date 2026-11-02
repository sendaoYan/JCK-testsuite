/*
 * @test
 * @summary javax.swing.text.AbstractDocument conformance: type shape via reflection (headless-safe).
 * @run main AbstractDocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument", false, AbstractDocumentTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.text.Document"), "implements javax.swing.text.Document");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDocumentProperties"), "method getDocumentProperties");
        check(names.contains("setDocumentProperties"), "method setDocumentProperties");
        check(names.contains("getListeners"), "method getListeners");
        check(names.contains("getAsynchronousLoadPriority"), "method getAsynchronousLoadPriority");
        check(names.contains("setAsynchronousLoadPriority"), "method setAsynchronousLoadPriority");
        check(names.contains("setDocumentFilter"), "method setDocumentFilter");
        check(names.contains("getDocumentFilter"), "method getDocumentFilter");
        check(names.contains("render"), "method render");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
