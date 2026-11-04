/*
 * @test
 * @summary javax.naming.CommunicationException conformance: message and hierarchy.
 * @run main CommunicationExceptionTest
 */


public class CommunicationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.CommunicationException e = new javax.naming.CommunicationException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.CommunicationException e0 = new javax.naming.CommunicationException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
