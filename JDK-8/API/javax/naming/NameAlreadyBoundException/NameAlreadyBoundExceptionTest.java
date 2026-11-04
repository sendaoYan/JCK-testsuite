/*
 * @test
 * @summary javax.naming.NameAlreadyBoundException conformance: message and hierarchy.
 * @run main NameAlreadyBoundExceptionTest
 */


public class NameAlreadyBoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.NameAlreadyBoundException e = new javax.naming.NameAlreadyBoundException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.NameAlreadyBoundException e0 = new javax.naming.NameAlreadyBoundException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
