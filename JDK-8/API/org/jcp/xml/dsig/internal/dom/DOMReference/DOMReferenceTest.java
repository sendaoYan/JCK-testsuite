/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMReference conformance: type shape via reflection (headless-safe).
 * @run main DOMReferenceTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMReference", false, DOMReferenceTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.Reference"), "implements javax.xml.crypto.dsig.Reference");
        check(hasInterface(c, "javax.xml.crypto.dom.DOMURIReference"), "implements javax.xml.crypto.dom.DOMURIReference");
        check(hasMethod(c, "digest"), "declares digest");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getCalculatedDigestValue"), "declares getCalculatedDigestValue");
        check(hasMethod(c, "getDereferencedData"), "declares getDereferencedData");
        check(hasMethod(c, "getDigestInputStream"), "declares getDigestInputStream");
        check(hasMethod(c, "getDigestMethod"), "declares getDigestMethod");
        check(hasMethod(c, "getDigestValue"), "declares getDigestValue");
        check(hasMethod(c, "getHere"), "declares getHere");
        check(hasField(c, "MAXIMUM_TRANSFORM_COUNT"), "declares field MAXIMUM_TRANSFORM_COUNT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
