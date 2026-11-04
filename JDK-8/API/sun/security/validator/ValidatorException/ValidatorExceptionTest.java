/*
 * @test
 * @summary sun.security.validator.ValidatorException conformance: type shape + behavioral (headless-safe).
 * @run main ValidatorExceptionTest
 */

public class ValidatorExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.validator.ValidatorException", false, ValidatorExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.cert.CertificateException"), "superclass");
        check(sun.security.validator.ValidatorException.T_NO_TRUST_ANCHOR != null, "ValidatorException T_NO_TRUST_ANCHOR");
        sun.security.validator.ValidatorException ve = new sun.security.validator.ValidatorException("bad");
        check(ve.getMessage() != null, "ValidatorException message");
        check(ve instanceof java.security.cert.CertificateException, "ValidatorException instanceof CertificateException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
