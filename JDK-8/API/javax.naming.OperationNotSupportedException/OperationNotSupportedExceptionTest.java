/*
 * @test
 * @summary javax.naming.OperationNotSupportedException conformance: message and hierarchy.
 * @run main OperationNotSupportedExceptionTest
 */


public class OperationNotSupportedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.OperationNotSupportedException e = new javax.naming.OperationNotSupportedException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.OperationNotSupportedException e0 = new javax.naming.OperationNotSupportedException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
