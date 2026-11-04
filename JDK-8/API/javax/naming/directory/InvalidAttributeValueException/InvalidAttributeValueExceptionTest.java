/*
 * @test
 * @summary javax.naming.directory.InvalidAttributeValueException conformance: message and hierarchy.
 * @run main InvalidAttributeValueExceptionTest
 */


public class InvalidAttributeValueExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.InvalidAttributeValueException e = new javax.naming.directory.InvalidAttributeValueException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.directory.InvalidAttributeValueException e0 = new javax.naming.directory.InvalidAttributeValueException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
