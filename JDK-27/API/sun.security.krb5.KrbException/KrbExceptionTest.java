/*
 * @test
 * @summary sun.security.krb5.KrbException conformance: safe value/exception behavior.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main KrbExceptionTest
 */
public class KrbExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        sun.security.krb5.KrbException e = new sun.security.krb5.KrbException(13);
        check(e.returnCode() == 13, "returnCode");
        sun.security.krb5.KrbException m = new sun.security.krb5.KrbException("boom");
        check(m.getMessage().equals("boom"), "getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
