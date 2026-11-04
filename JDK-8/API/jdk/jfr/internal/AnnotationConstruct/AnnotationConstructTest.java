/*
 * @test
 * @summary jdk.jfr.internal.AnnotationConstruct conformance: type shape via reflection (headless-safe).
 * @run main AnnotationConstructTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AnnotationConstructTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.AnnotationConstruct", false, AnnotationConstructTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getAnnotation"), "declares getAnnotation");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getLabel"), "declares getLabel");
        check(hasMethod(c, "getUnmodifiableAnnotationElements"), "declares getUnmodifiableAnnotationElements");
        check(hasMethod(c, "hasUnsigned"), "declares hasUnsigned");
        check(hasMethod(c, "setAnnotationElements"), "declares setAnnotationElements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
