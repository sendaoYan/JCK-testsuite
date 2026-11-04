/*
 * @test
 * @summary jdk.jfr.internal.Type conformance: type shape via reflection (headless-safe).
 * @run main TypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.Type", false, TypeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "java.lang.Comparable"), "implements java.lang.Comparable");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "compareTo"), "declares compareTo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getAnnotation"), "declares getAnnotation");
        check(hasMethod(c, "getAnnotationElements"), "declares getAnnotationElements");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getFields"), "declares getFields");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasField(c, "EVENT_NAME_PREFIX"), "declares field EVENT_NAME_PREFIX");
        check(hasField(c, "SETTINGS_PREFIX"), "declares field SETTINGS_PREFIX");
        check(hasField(c, "SUPER_TYPE_ANNOTATION"), "declares field SUPER_TYPE_ANNOTATION");
        check(hasField(c, "SUPER_TYPE_EVENT"), "declares field SUPER_TYPE_EVENT");
        check(hasField(c, "SUPER_TYPE_SETTING"), "declares field SUPER_TYPE_SETTING");
        check(hasField(c, "TYPES_PREFIX"), "declares field TYPES_PREFIX");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
