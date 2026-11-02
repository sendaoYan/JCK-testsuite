/*
 * @test
 * @summary javax.naming.ContextNotEmptyException conformance: message and hierarchy.
 * @run main ContextNotEmptyExceptionTest
 */


public class ContextNotEmptyExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ContextNotEmptyException e = new javax.naming.ContextNotEmptyException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.ContextNotEmptyException e0 = new javax.naming.ContextNotEmptyException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
