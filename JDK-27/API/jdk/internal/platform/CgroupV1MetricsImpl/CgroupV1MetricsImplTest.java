/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.CgroupV1MetricsImpl conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform:+open
 * @run main CgroupV1MetricsImplTest
 */
public class CgroupV1MetricsImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupV1MetricsImpl", false, CgroupV1MetricsImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.platform.CgroupMetrics"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "jdk.internal.platform.CgroupV1Metrics"), "interface jdk.internal.platform.CgroupV1Metrics");
        check(hasMethod(c, "getCpuSetMemoryPressure"), "method getCpuSetMemoryPressure");
        check(hasMethod(c, "getKernelMemoryFailCount"), "method getKernelMemoryFailCount");
        check(hasMethod(c, "getKernelMemoryMaxUsage"), "method getKernelMemoryMaxUsage");
        check(hasMethod(c, "getKernelMemoryUsage"), "method getKernelMemoryUsage");
        check(hasMethod(c, "getMemoryAndSwapFailCount"), "method getMemoryAndSwapFailCount");
        check(hasMethod(c, "getMemoryAndSwapMaxUsage"), "method getMemoryAndSwapMaxUsage");
        check(hasMethod(c, "getMemoryMaxUsage"), "method getMemoryMaxUsage");
        check(hasMethod(c, "getTcpMemoryFailCount"), "method getTcpMemoryFailCount");
        check(hasMethod(c, "getTcpMemoryMaxUsage"), "method getTcpMemoryMaxUsage");
        check(hasMethod(c, "isCpuSetMemoryPressureEnabled"), "method isCpuSetMemoryPressureEnabled");
        check(hasMethod(c, "isMemoryOOMKillEnabled"), "method isMemoryOOMKillEnabled");
        check(hasField(c, "metrics"), "field metrics");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
