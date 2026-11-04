/*
 * @test
 * @summary com.sun.management.ThreadMXBean conformance: hierarchy and declared members via reflection.
 * @run main ThreadMXBeanTest
 */

public class ThreadMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.management.ThreadMXBean", false, null);
        check(c.getName().equals("com.sun.management.ThreadMXBean"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.lang.management.ThreadMXBean"), "implements java.lang.management.ThreadMXBean");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("dumpAllThreads"), "declares dumpAllThreads");
        check(ms.contains("getCurrentThreadAllocatedBytes"), "declares getCurrentThreadAllocatedBytes");
        check(ms.contains("getThreadAllocatedBytes"), "declares getThreadAllocatedBytes");
        check(ms.contains("getThreadCpuTime"), "declares getThreadCpuTime");
        check(ms.contains("getThreadInfo"), "declares getThreadInfo");
        check(ms.contains("getThreadUserTime"), "declares getThreadUserTime");
        check(ms.contains("isThreadAllocatedMemoryEnabled"), "declares isThreadAllocatedMemoryEnabled");
        check(ms.contains("isThreadAllocatedMemorySupported"), "declares isThreadAllocatedMemorySupported");
        check(ms.contains("setThreadAllocatedMemoryEnabled"), "declares setThreadAllocatedMemoryEnabled");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
