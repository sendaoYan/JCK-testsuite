/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.PSVIProvider conformance: interface hierarchy and members.
 * @run main PSVIProviderTest
 */
public class PSVIProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.PSVIProvider", false, PSVIProviderTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getElementPSVI"), "method getElementPSVI");
        check(mn.contains("getAttributePSVI"), "method getAttributePSVI");
        check(mn.contains("getAttributePSVIByName"), "method getAttributePSVIByName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
