/*
 * @test
 * @summary sun.security.krb5.RealmException conformance: safe value/exception behavior.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main RealmExceptionTest
 */
public class RealmExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        sun.security.krb5.RealmException e = new sun.security.krb5.RealmException(15);
        check(e.returnCode() == 15, "returnCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
