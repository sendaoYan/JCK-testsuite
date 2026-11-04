/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMUtils conformance: type shape via reflection (headless-safe).
 * @run main DOMUtilsTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMUtils", false, DOMUtilsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "appendChild"), "declares appendChild");
        check(hasMethod(c, "createElement"), "declares createElement");
        check(hasMethod(c, "getAttributeValue"), "declares getAttributeValue");
        check(hasMethod(c, "getFirstChildElement"), "declares getFirstChildElement");
        check(hasMethod(c, "getIdAttributeValue"), "declares getIdAttributeValue");
        check(hasMethod(c, "getLastChildElement"), "declares getLastChildElement");
        check(hasMethod(c, "getNSPrefix"), "declares getNSPrefix");
        check(hasMethod(c, "getNextSiblingElement"), "declares getNextSiblingElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
