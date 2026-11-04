/*
 * @test
 * @summary jdk.jfr.FlightRecorder conformance: type shape via reflection (headless-safe).
 * @run main FlightRecorderTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FlightRecorderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorder", false, FlightRecorderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "addListener"), "declares addListener");
        check(hasMethod(c, "addPeriodicEvent"), "declares addPeriodicEvent");
        check(hasMethod(c, "getEventTypes"), "declares getEventTypes");
        check(hasMethod(c, "getFlightRecorder"), "declares getFlightRecorder");
        check(hasMethod(c, "getRecordings"), "declares getRecordings");
        check(hasMethod(c, "isAvailable"), "declares isAvailable");
        check(hasMethod(c, "isInitialized"), "declares isInitialized");
        check(hasMethod(c, "register"), "declares register");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
