/*
 * @test
 * @summary javax.naming.directory.AttributeModificationException conformance: message and hierarchy.
 * @run main AttributeModificationExceptionTest
 */


public class AttributeModificationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.AttributeModificationException e = new javax.naming.directory.AttributeModificationException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.directory.AttributeModificationException e0 = new javax.naming.directory.AttributeModificationException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
