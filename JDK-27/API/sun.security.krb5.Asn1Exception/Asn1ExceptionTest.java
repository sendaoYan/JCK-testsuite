/*
 * @test
 * @summary sun.security.krb5.Asn1Exception conformance: safe value/exception behavior.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main Asn1ExceptionTest
 */
public class Asn1ExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        sun.security.krb5.Asn1Exception e = new sun.security.krb5.Asn1Exception(14);
        check(e.returnCode() == 14, "returnCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
