/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.cgroupv1.CgroupV1Subsystem conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform.cgroupv1:+open
 * @run main CgroupV1SubsystemTest
 */
public class CgroupV1SubsystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.cgroupv1.CgroupV1Subsystem", false, CgroupV1SubsystemTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 2, "interface count");
        check(hasInterface(c, "jdk.internal.platform.CgroupSubsystem"), "interface jdk.internal.platform.CgroupSubsystem");
        check(hasInterface(c, "jdk.internal.platform.CgroupV1Metrics"), "interface jdk.internal.platform.CgroupV1Metrics");
        check(hasMethod(c, "getBlkIOServiceCount"), "method getBlkIOServiceCount");
        check(hasMethod(c, "getBlkIOServiced"), "method getBlkIOServiced");
        check(hasMethod(c, "getCpuNumPeriods"), "method getCpuNumPeriods");
        check(hasMethod(c, "getCpuNumThrottled"), "method getCpuNumThrottled");
        check(hasMethod(c, "getCpuPeriod"), "method getCpuPeriod");
        check(hasMethod(c, "getCpuQuota"), "method getCpuQuota");
        check(hasMethod(c, "getCpuSetCpus"), "method getCpuSetCpus");
        check(hasMethod(c, "getCpuSetMemoryPressure"), "method getCpuSetMemoryPressure");
        check(hasMethod(c, "getCpuSetMems"), "method getCpuSetMems");
        check(hasMethod(c, "getCpuShares"), "method getCpuShares");
        check(hasMethod(c, "getCpuSystemUsage"), "method getCpuSystemUsage");
        check(hasMethod(c, "getCpuThrottledTime"), "method getCpuThrottledTime");
        check(hasMethod(c, "getCpuUsage"), "method getCpuUsage");
        check(hasMethod(c, "getCpuUserUsage"), "method getCpuUserUsage");
        check(hasMethod(c, "getEffectiveCpuCount"), "method getEffectiveCpuCount");
        check(hasMethod(c, "getEffectiveCpuSetCpus"), "method getEffectiveCpuSetCpus");
        check(hasMethod(c, "getEffectiveCpuSetMems"), "method getEffectiveCpuSetMems");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getKernelMemoryFailCount"), "method getKernelMemoryFailCount");
        check(hasMethod(c, "getKernelMemoryMaxUsage"), "method getKernelMemoryMaxUsage");
        check(hasMethod(c, "getKernelMemoryUsage"), "method getKernelMemoryUsage");
        check(hasMethod(c, "getMemoryAndSwapFailCount"), "method getMemoryAndSwapFailCount");
        check(hasMethod(c, "getMemoryAndSwapLimit"), "method getMemoryAndSwapLimit");
        check(hasMethod(c, "getMemoryAndSwapMaxUsage"), "method getMemoryAndSwapMaxUsage");
        check(hasMethod(c, "getMemoryAndSwapUsage"), "method getMemoryAndSwapUsage");
        check(hasMethod(c, "getMemoryFailCount"), "method getMemoryFailCount");
        check(hasMethod(c, "getMemoryLimit"), "method getMemoryLimit");
        check(hasMethod(c, "getMemoryMaxUsage"), "method getMemoryMaxUsage");
        check(hasMethod(c, "getMemorySoftLimit"), "method getMemorySoftLimit");
        check(hasMethod(c, "getMemoryUsage"), "method getMemoryUsage");
        check(hasMethod(c, "getPerCpuUsage"), "method getPerCpuUsage");
        check(hasMethod(c, "getPidsCurrent"), "method getPidsCurrent");
        check(hasMethod(c, "getPidsMax"), "method getPidsMax");
        check(hasMethod(c, "getProvider"), "method getProvider");
        check(hasMethod(c, "getTcpMemoryFailCount"), "method getTcpMemoryFailCount");
        check(hasMethod(c, "getTcpMemoryMaxUsage"), "method getTcpMemoryMaxUsage");
        check(hasMethod(c, "getTcpMemoryUsage"), "method getTcpMemoryUsage");
        check(hasMethod(c, "isCpuSetMemoryPressureEnabled"), "method isCpuSetMemoryPressureEnabled");
        check(hasMethod(c, "isMemoryOOMKillEnabled"), "method isMemoryOOMKillEnabled");
        check(hasField(c, "INSTANCE"), "field INSTANCE");
        check(hasField(c, "PROVIDER_NAME"), "field PROVIDER_NAME");
        check(hasField(c, "blkio"), "field blkio");
        check(hasField(c, "cpu"), "field cpu");
        check(hasField(c, "cpuacct"), "field cpuacct");
        check(hasField(c, "cpuset"), "field cpuset");
        check(hasField(c, "memory"), "field memory");
        check(hasField(c, "pids"), "field pids");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
