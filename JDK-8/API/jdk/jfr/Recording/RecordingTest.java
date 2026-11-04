/*
 * @test
 * @summary jdk.jfr.Recording conformance: type shape via reflection (headless-safe).
 * @run main RecordingTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RecordingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Recording", false, RecordingTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "java.io.Closeable"), "implements java.io.Closeable");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "copy"), "declares copy");
        check(hasMethod(c, "disable"), "declares disable");
        check(hasMethod(c, "dump"), "declares dump");
        check(hasMethod(c, "enable"), "declares enable");
        check(hasMethod(c, "getDestination"), "declares getDestination");
        check(hasMethod(c, "getDumpOnExit"), "declares getDumpOnExit");
        check(hasMethod(c, "getDuration"), "declares getDuration");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
