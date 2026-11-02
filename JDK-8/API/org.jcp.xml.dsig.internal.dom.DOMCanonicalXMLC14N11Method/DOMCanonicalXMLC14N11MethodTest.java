/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method conformance: type shape via reflection (headless-safe).
 * @run main DOMCanonicalXMLC14N11MethodTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMCanonicalXMLC14N11MethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method", false, DOMCanonicalXMLC14N11MethodTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer"), "superclass org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer");
        check(hasMethod(c, "init"), "declares init");
        check(hasMethod(c, "transform"), "declares transform");
        check(hasField(c, "C14N_11"), "declares field C14N_11");
        check(hasField(c, "C14N_11_WITH_COMMENTS"), "declares field C14N_11_WITH_COMMENTS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
