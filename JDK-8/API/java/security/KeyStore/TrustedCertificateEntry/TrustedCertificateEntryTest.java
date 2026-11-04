/*
 * @test
 * @summary java.security.KeyStore.TrustedCertificateEntry conformance: class hierarchy and methods.
 * @run main TrustedCertificateEntryTest
 */

public class TrustedCertificateEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$TrustedCertificateEntry");
        check(!c.isInterface(), "is class");
        check(java.security.KeyStore.Entry.class.isAssignableFrom(c), "implements KeyStore.Entry");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getTrustedCertificate"), "declares getTrustedCertificate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
