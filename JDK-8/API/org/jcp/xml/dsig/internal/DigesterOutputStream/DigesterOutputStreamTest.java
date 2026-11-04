/*
 * @test
 * @summary org.jcp.xml.dsig.internal.DigesterOutputStream conformance: type shape via reflection (headless-safe).
 * @run main DigesterOutputStreamTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DigesterOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.DigesterOutputStream", false, DigesterOutputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.OutputStream"), "superclass java.io.OutputStream");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "getDigestValue"), "declares getDigestValue");
        check(hasMethod(c, "getInputStream"), "declares getInputStream");
        check(hasMethod(c, "write"), "declares write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
