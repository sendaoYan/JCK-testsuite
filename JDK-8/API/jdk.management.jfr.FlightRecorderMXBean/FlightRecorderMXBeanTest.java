/*
 * @test
 * @summary jdk.management.jfr.FlightRecorderMXBean conformance: reflection shape checks.
 * @run main FlightRecorderMXBeanTest
 */

import java.lang.reflect.*;

public class FlightRecorderMXBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.FlightRecorderMXBean");
        check(c != null, "jdk.management.jfr.FlightRecorderMXBean loads");
        check(c.getName().equals("jdk.management.jfr.FlightRecorderMXBean"), "binary name is jdk.management.jfr.FlightRecorderMXBean");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.FlightRecorderMXBean is public");
        check(c.isInterface(), "jdk.management.jfr.FlightRecorderMXBean is an interface");
        check(ifaceNames(c).equals(set("java.lang.management.PlatformManagedObject")), "direct superinterfaces");
        check(declares(c, "newRecording"), "declares newRecording");
        check(declares(c, "takeSnapshot"), "declares takeSnapshot");
        check(declares(c, "cloneRecording"), "declares cloneRecording");
        check(declares(c, "startRecording"), "declares startRecording");
        check(declares(c, "stopRecording"), "declares stopRecording");
        check(declares(c, "closeRecording"), "declares closeRecording");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
