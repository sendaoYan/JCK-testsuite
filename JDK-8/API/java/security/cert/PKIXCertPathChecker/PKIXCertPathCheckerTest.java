/*
 * @test
 * @summary java.security.cert.PKIXCertPathChecker conformance: abstract class hierarchy and key methods.
 * @run main PKIXCertPathCheckerTest
 */

public class PKIXCertPathCheckerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.PKIXCertPathChecker");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.cert.CertPathChecker"), "implements java.security.cert.CertPathChecker");
        check(in.contains("java.lang.Cloneable"), "implements java.lang.Cloneable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("check"), "declares check");
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("getSupportedExtensions"), "declares getSupportedExtensions");
        check(ms.contains("init"), "declares init");
        check(ms.contains("isForwardCheckingSupported"), "declares isForwardCheckingSupported");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
