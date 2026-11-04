/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod conformance: type shape via reflection (headless-safe).
 * @run main DOMExcC14NMethodTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMExcC14NMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod", false, DOMExcC14NMethodTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer"), "superclass org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer");
        check(hasMethod(c, "getParameterSpecPrefixList"), "declares getParameterSpecPrefixList");
        check(hasMethod(c, "getParamsNSURI"), "declares getParamsNSURI");
        check(hasMethod(c, "init"), "declares init");
        check(hasMethod(c, "marshalParams"), "declares marshalParams");
        check(hasMethod(c, "transform"), "declares transform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
