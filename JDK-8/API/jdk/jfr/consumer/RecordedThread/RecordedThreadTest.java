/*
 * @test
 * @summary jdk.jfr.consumer.RecordedThread conformance: type shape via reflection (headless-safe).
 * @run main RecordedThreadTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RecordedThreadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedThread", false, RecordedThreadTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getJavaName"), "declares getJavaName");
        check(hasMethod(c, "getJavaThreadId"), "declares getJavaThreadId");
        check(hasMethod(c, "getOSName"), "declares getOSName");
        check(hasMethod(c, "getOSThreadId"), "declares getOSThreadId");
        check(hasMethod(c, "getThreadGroup"), "declares getThreadGroup");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
