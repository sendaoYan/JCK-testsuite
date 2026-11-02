/*
 * @test
 * @summary jdk.jfr.consumer.RecordedEvent conformance: type shape via reflection (headless-safe).
 * @run main RecordedEventTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RecordedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedEvent", false, RecordedEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getDuration"), "declares getDuration");
        check(hasMethod(c, "getEndTime"), "declares getEndTime");
        check(hasMethod(c, "getEventType"), "declares getEventType");
        check(hasMethod(c, "getFields"), "declares getFields");
        check(hasMethod(c, "getStackTrace"), "declares getStackTrace");
        check(hasMethod(c, "getStartTime"), "declares getStartTime");
        check(hasMethod(c, "getThread"), "declares getThread");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
