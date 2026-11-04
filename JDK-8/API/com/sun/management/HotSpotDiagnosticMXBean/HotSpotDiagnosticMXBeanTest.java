/*
 * @test
 * @summary com.sun.management.HotSpotDiagnosticMXBean conformance: hierarchy and declared members via reflection.
 * @run main HotSpotDiagnosticMXBeanTest
 */

public class HotSpotDiagnosticMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.management.HotSpotDiagnosticMXBean", false, null);
        check(c.getName().equals("com.sun.management.HotSpotDiagnosticMXBean"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.lang.management.PlatformManagedObject"), "implements java.lang.management.PlatformManagedObject");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("dumpHeap"), "declares dumpHeap");
        check(ms.contains("getDiagnosticOptions"), "declares getDiagnosticOptions");
        check(ms.contains("getVMOption"), "declares getVMOption");
        check(ms.contains("setVMOption"), "declares setVMOption");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
