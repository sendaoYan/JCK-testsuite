/*
 * @test
 * @summary java.security.cert.PKIXRevocationChecker conformance: abstract class hierarchy and key methods.
 * @run main PKIXRevocationCheckerTest
 */

public class PKIXRevocationCheckerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.PKIXRevocationChecker");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.cert.PKIXCertPathChecker"), "extends java.security.cert.PKIXCertPathChecker");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("getOcspExtensions"), "declares getOcspExtensions");
        check(ms.contains("getOcspResponder"), "declares getOcspResponder");
        check(ms.contains("getOcspResponderCert"), "declares getOcspResponderCert");
        check(ms.contains("getOptions"), "declares getOptions");
        check(ms.contains("getSoftFailExceptions"), "declares getSoftFailExceptions");
        check(ms.contains("setOcspExtensions"), "declares setOcspExtensions");
        check(ms.contains("setOcspResponder"), "declares setOcspResponder");
        check(ms.contains("setOcspResponderCert"), "declares setOcspResponderCert");
        check(ms.contains("setOcspResponses"), "declares setOcspResponses");
        check(ms.contains("setOptions"), "declares setOptions");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
