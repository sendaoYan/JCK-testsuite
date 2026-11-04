/*
 * @test
 * @summary java.net.PasswordAuthentication conformance: userName/password copy.
 * @run main PasswordAuthenticationTest
 */


public class PasswordAuthenticationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.PasswordAuthentication p = new java.net.PasswordAuthentication("alice", new char[]{'s','e','c'});
        check("alice".equals(p.getUserName()), "getUserName");
        char[] pw = p.getPassword();
        check(pw.length == 3 && pw[0] == 's' && pw[1] == 'e' && pw[2] == 'c', "getPassword");        System.out.println("PASS (" + passed + " assertions)");
    }
}
