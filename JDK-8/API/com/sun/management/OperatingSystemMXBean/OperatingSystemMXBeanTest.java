/*
 * @test
 * @summary com.sun.management.OperatingSystemMXBean conformance: hierarchy and declared members via reflection.
 * @run main OperatingSystemMXBeanTest
 */

public class OperatingSystemMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.management.OperatingSystemMXBean", false, null);
        check(c.getName().equals("com.sun.management.OperatingSystemMXBean"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.lang.management.OperatingSystemMXBean"), "implements java.lang.management.OperatingSystemMXBean");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCommittedVirtualMemorySize"), "declares getCommittedVirtualMemorySize");
        check(ms.contains("getFreePhysicalMemorySize"), "declares getFreePhysicalMemorySize");
        check(ms.contains("getFreeSwapSpaceSize"), "declares getFreeSwapSpaceSize");
        check(ms.contains("getProcessCpuLoad"), "declares getProcessCpuLoad");
        check(ms.contains("getProcessCpuTime"), "declares getProcessCpuTime");
        check(ms.contains("getSystemCpuLoad"), "declares getSystemCpuLoad");
        check(ms.contains("getTotalPhysicalMemorySize"), "declares getTotalPhysicalMemorySize");
        check(ms.contains("getTotalSwapSpaceSize"), "declares getTotalSwapSpaceSize");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
