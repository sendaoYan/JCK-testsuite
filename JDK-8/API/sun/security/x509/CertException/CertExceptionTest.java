/*
 * @test
 * @summary sun.security.x509.CertException conformance: type shape + behavioral (headless-safe).
 * @run main CertExceptionTest
 */

public class CertExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.x509.CertException", false, CertExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.SecurityException"), "superclass");
        sun.security.x509.CertException ce = new sun.security.x509.CertException(sun.security.x509.CertException.verf_INVALID_SIG);
        check(ce.getVerfCode() == sun.security.x509.CertException.verf_INVALID_SIG, "CertException verf code");
        check(ce.getVerfDescription() != null, "CertException verf description");
        check(ce instanceof java.lang.SecurityException, "CertException instanceof SecurityException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
