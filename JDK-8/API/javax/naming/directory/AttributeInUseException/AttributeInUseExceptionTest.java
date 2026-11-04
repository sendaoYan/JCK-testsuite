/*
 * @test
 * @summary javax.naming.directory.AttributeInUseException conformance: message and hierarchy.
 * @run main AttributeInUseExceptionTest
 */


public class AttributeInUseExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.AttributeInUseException e = new javax.naming.directory.AttributeInUseException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.directory.AttributeInUseException e0 = new javax.naming.directory.AttributeInUseException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
