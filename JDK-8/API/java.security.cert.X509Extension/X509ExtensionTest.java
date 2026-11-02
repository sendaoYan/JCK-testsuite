/*
 * @test
 * @summary java.security.cert.X509Extension conformance: interface hierarchy and key methods.
 * @run main X509ExtensionTest
 */

public class X509ExtensionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.X509Extension");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCriticalExtensionOIDs"), "declares getCriticalExtensionOIDs");
        check(ms.contains("getExtensionValue"), "declares getExtensionValue");
        check(ms.contains("getNonCriticalExtensionOIDs"), "declares getNonCriticalExtensionOIDs");
        check(ms.contains("hasUnsupportedCriticalExtension"), "declares hasUnsupportedCriticalExtension");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
