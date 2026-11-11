/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.CgroupV1Metrics conformance: type shape via reflection (headless-safe).
 * @run main CgroupV1MetricsTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CgroupV1MetricsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupV1Metrics", false, CgroupV1MetricsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "jdk.internal.platform.Metrics"), "implements jdk.internal.platform.Metrics");
        check(hasMethod(c, "getCpuSetMemoryPressure"), "declares getCpuSetMemoryPressure");
        check(hasMethod(c, "getKernelMemoryFailCount"), "declares getKernelMemoryFailCount");
        check(hasMethod(c, "getKernelMemoryLimit"), "declares getKernelMemoryLimit");
        check(hasMethod(c, "getKernelMemoryMaxUsage"), "declares getKernelMemoryMaxUsage");
        check(hasMethod(c, "getKernelMemoryUsage"), "declares getKernelMemoryUsage");
        check(hasMethod(c, "getMemoryAndSwapFailCount"), "declares getMemoryAndSwapFailCount");
        check(hasMethod(c, "getMemoryAndSwapMaxUsage"), "declares getMemoryAndSwapMaxUsage");
        check(hasMethod(c, "getMemoryMaxUsage"), "declares getMemoryMaxUsage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
