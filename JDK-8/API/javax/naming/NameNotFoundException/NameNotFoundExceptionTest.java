/*
 * @test
 * @summary javax.naming.NameNotFoundException conformance: message and hierarchy.
 * @run main NameNotFoundExceptionTest
 */


public class NameNotFoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.NameNotFoundException e = new javax.naming.NameNotFoundException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.NameNotFoundException e0 = new javax.naming.NameNotFoundException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
