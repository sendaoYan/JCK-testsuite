/*
 * @test
 * @summary jdk.management.VirtualThreadSchedulerMXBean conformance: interface.
 * @run main VirtualThreadSchedulerMXBeanTest
 */

import java.lang.reflect.*;


public class VirtualThreadSchedulerMXBeanTest {
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
        Class<?> c = Class.forName("jdk.management.VirtualThreadSchedulerMXBean");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.lang.management.PlatformManagedObject"), "extends java.lang.management.PlatformManagedObject");
        check(hasMethod(c, "getParallelism"), "declares getParallelism");
        check(hasMethod(c, "setParallelism"), "declares setParallelism");
        check(hasMethod(c, "getPoolSize"), "declares getPoolSize");
        check(hasMethod(c, "getMountedVirtualThreadCount"), "declares getMountedVirtualThreadCount");
        check(hasMethod(c, "getQueuedVirtualThreadCount"), "declares getQueuedVirtualThreadCount");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
