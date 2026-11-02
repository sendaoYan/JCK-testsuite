/*
 * @test
 * @summary java.security.cert.CertificateFactorySpi conformance: abstract class hierarchy and key methods.
 * @run main CertificateFactorySpiTest
 */

public class CertificateFactorySpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.CertificateFactorySpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineGenerateCRL"), "declares engineGenerateCRL");
        check(ms.contains("engineGenerateCertPath"), "declares engineGenerateCertPath");
        check(ms.contains("engineGenerateCertificate"), "declares engineGenerateCertificate");
        check(ms.contains("engineGetCertPathEncodings"), "declares engineGetCertPathEncodings");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
