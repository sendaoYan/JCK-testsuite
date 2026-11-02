/*
 * @test
 * @summary jdk.management.HotSpotAOTCacheMXBean conformance: interface.
 * @run main HotSpotAOTCacheMXBeanTest
 */

import java.lang.reflect.*;


public class HotSpotAOTCacheMXBeanTest {
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
        Class<?> c = Class.forName("jdk.management.HotSpotAOTCacheMXBean");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.lang.management.PlatformManagedObject"), "extends java.lang.management.PlatformManagedObject");
        check(hasMethod(c, "endRecording"), "declares endRecording");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
