/*
 * @test
 * @summary jdk.management.jfr.FlightRecorderMXBean conformance: interface.
 * @run main FlightRecorderMXBeanTest
 */

import java.lang.reflect.*;


public class FlightRecorderMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.FlightRecorderMXBean");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.lang.management.PlatformManagedObject"), "extends java.lang.management.PlatformManagedObject");
        check(hasMethod(c, "newRecording"), "declares newRecording");
        check(hasMethod(c, "takeSnapshot"), "declares takeSnapshot");
        check(hasMethod(c, "cloneRecording"), "declares cloneRecording");
        check(hasMethod(c, "startRecording"), "declares startRecording");
        check(hasMethod(c, "stopRecording"), "declares stopRecording");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
