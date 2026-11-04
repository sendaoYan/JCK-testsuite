/*
 * @test
 * @summary sun.security.krb5.Asn1Exception conformance: type shape + behavioral (headless-safe).
 * @run main Asn1ExceptionTest
 */

public class Asn1ExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.Asn1Exception", false, Asn1ExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.krb5.KrbException"), "superclass");
        sun.security.krb5.Asn1Exception ae = new sun.security.krb5.Asn1Exception(0);
        check(ae instanceof sun.security.krb5.KrbException, "Asn1Exception instanceof KrbException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
