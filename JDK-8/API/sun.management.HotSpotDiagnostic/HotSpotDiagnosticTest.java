/*
 * @test
 * @summary sun.management.HotSpotDiagnostic conformance: class and members.
 * @run main HotSpotDiagnosticTest
 */

public class HotSpotDiagnosticTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.HotSpotDiagnostic", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.HotSpotDiagnostic"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("com.sun.management.HotSpotDiagnosticMXBean"), "interface com.sun.management.HotSpotDiagnosticMXBean");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("dumpHeap"), "declares method dumpHeap");
        check(mnames.contains("dumpHeap0"), "declares method dumpHeap0");
        check(mnames.contains("getDiagnosticOptions"), "declares method getDiagnosticOptions");
        check(mnames.contains("getObjectName"), "declares method getObjectName");
        check(mnames.contains("getVMOption"), "declares method getVMOption");
        check(mnames.contains("setVMOption"), "declares method setVMOption");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
