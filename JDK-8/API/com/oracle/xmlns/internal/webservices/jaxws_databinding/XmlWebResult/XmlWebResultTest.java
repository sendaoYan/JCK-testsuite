/*
 * @test
 * @summary com.oracle.xmlns.internal.webservices.jaxws_databinding.XmlWebResult conformance: type shape via reflection (headless-safe).
 * @run main XmlWebResultTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class XmlWebResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.oracle.xmlns.internal.webservices.jaxws_databinding.XmlWebResult", false, XmlWebResultTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "javax.jws.WebResult"), "implements javax.jws.WebResult");
        check(hasMethod(c, "annotationType"), "declares annotationType");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getPartName"), "declares getPartName");
        check(hasMethod(c, "getTargetNamespace"), "declares getTargetNamespace");
        check(hasMethod(c, "header"), "declares header");
        check(hasMethod(c, "isHeader"), "declares isHeader");
        check(hasMethod(c, "name"), "declares name");
        check(hasMethod(c, "partName"), "declares partName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
