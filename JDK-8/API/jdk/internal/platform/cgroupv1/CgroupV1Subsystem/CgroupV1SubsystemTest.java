/*
 * @test
 * @summary jdk.internal.platform.cgroupv1.CgroupV1Subsystem conformance: type shape via reflection (headless-safe).
 * @run main CgroupV1SubsystemTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CgroupV1SubsystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.cgroupv1.CgroupV1Subsystem", false, CgroupV1SubsystemTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "jdk.internal.platform.CgroupSubsystem"), "implements jdk.internal.platform.CgroupSubsystem");
        check(hasInterface(c, "jdk.internal.platform.CgroupV1Metrics"), "implements jdk.internal.platform.CgroupV1Metrics");
        check(hasMethod(c, "getBlkIOServiceCount"), "declares getBlkIOServiceCount");
        check(hasMethod(c, "getBlkIOServiced"), "declares getBlkIOServiced");
        check(hasMethod(c, "getCpuNumPeriods"), "declares getCpuNumPeriods");
        check(hasMethod(c, "getCpuNumThrottled"), "declares getCpuNumThrottled");
        check(hasMethod(c, "getCpuPeriod"), "declares getCpuPeriod");
        check(hasMethod(c, "getCpuQuota"), "declares getCpuQuota");
        check(hasMethod(c, "getCpuSetCpus"), "declares getCpuSetCpus");
        check(hasMethod(c, "getCpuSetMemoryPressure"), "declares getCpuSetMemoryPressure");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
