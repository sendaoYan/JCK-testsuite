/*
 * @test
 * @summary jdk.internal.platform.Metrics conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform:+open
 * @run main MetricsTest
 */
public class MetricsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.Metrics", false, MetricsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getBlkIOServiceCount"), "method getBlkIOServiceCount");
        check(hasMethod(c, "getBlkIOServiced"), "method getBlkIOServiced");
        check(hasMethod(c, "getCpuNumPeriods"), "method getCpuNumPeriods");
        check(hasMethod(c, "getCpuNumThrottled"), "method getCpuNumThrottled");
        check(hasMethod(c, "getCpuPeriod"), "method getCpuPeriod");
        check(hasMethod(c, "getCpuQuota"), "method getCpuQuota");
        check(hasMethod(c, "getCpuSetCpus"), "method getCpuSetCpus");
        check(hasMethod(c, "getCpuSetMems"), "method getCpuSetMems");
        check(hasMethod(c, "getCpuShares"), "method getCpuShares");
        check(hasMethod(c, "getCpuSystemUsage"), "method getCpuSystemUsage");
        check(hasMethod(c, "getCpuThrottledTime"), "method getCpuThrottledTime");
        check(hasMethod(c, "getCpuUsage"), "method getCpuUsage");
        check(hasMethod(c, "getCpuUserUsage"), "method getCpuUserUsage");
        check(hasMethod(c, "getEffectiveCpuCount"), "method getEffectiveCpuCount");
        check(hasMethod(c, "getEffectiveCpuSetCpus"), "method getEffectiveCpuSetCpus");
        check(hasMethod(c, "getEffectiveCpuSetMems"), "method getEffectiveCpuSetMems");
        check(hasMethod(c, "getMemoryAndSwapLimit"), "method getMemoryAndSwapLimit");
        check(hasMethod(c, "getMemoryAndSwapUsage"), "method getMemoryAndSwapUsage");
        check(hasMethod(c, "getMemoryFailCount"), "method getMemoryFailCount");
        check(hasMethod(c, "getMemoryLimit"), "method getMemoryLimit");
        check(hasMethod(c, "getMemorySoftLimit"), "method getMemorySoftLimit");
        check(hasMethod(c, "getMemoryUsage"), "method getMemoryUsage");
        check(hasMethod(c, "getPerCpuUsage"), "method getPerCpuUsage");
        check(hasMethod(c, "getPidsCurrent"), "method getPidsCurrent");
        check(hasMethod(c, "getPidsMax"), "method getPidsMax");
        check(hasMethod(c, "getProvider"), "method getProvider");
        check(hasMethod(c, "getTcpMemoryUsage"), "method getTcpMemoryUsage");
        check(hasMethod(c, "isContainerized"), "method isContainerized");
        check(hasMethod(c, "systemMetrics"), "method systemMetrics");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
