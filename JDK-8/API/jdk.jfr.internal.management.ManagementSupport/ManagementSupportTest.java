/*
 * @test
 * @summary jdk.jfr.internal.management.ManagementSupport conformance: type shape via reflection (headless-safe).
 * @run main ManagementSupportTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ManagementSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.management.ManagementSupport", false, ManagementSupportTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "formatTimespan"), "declares formatTimespan");
        check(hasMethod(c, "getDestinationOriginalText"), "declares getDestinationOriginalText");
        check(hasMethod(c, "getEventTypes"), "declares getEventTypes");
        check(hasMethod(c, "logError"), "declares logError");
        check(hasMethod(c, "parseTimespan"), "declares parseTimespan");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
