/*
 * @test
 * @summary jdk.jfr.internal.PlatformEventType conformance: type shape via reflection (headless-safe).
 * @run main PlatformEventTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PlatformEventTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.PlatformEventType", false, PlatformEventTypeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.internal.Type"), "superclass jdk.jfr.internal.Type");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "getAllSettings"), "declares getAllSettings");
        check(hasMethod(c, "getPeriod"), "declares getPeriod");
        check(hasMethod(c, "getSettings"), "declares getSettings");
        check(hasMethod(c, "getStackTraceEnabled"), "declares getStackTraceEnabled");
        check(hasMethod(c, "getStackTraceOffset"), "declares getStackTraceOffset");
        check(hasMethod(c, "getThresholdTicks"), "declares getThresholdTicks");
        check(hasMethod(c, "hasCutoff"), "declares hasCutoff");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
