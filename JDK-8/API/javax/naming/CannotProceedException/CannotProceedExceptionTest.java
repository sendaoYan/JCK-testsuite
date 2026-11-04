/*
 * @test
 * @summary javax.naming.CannotProceedException conformance: message and hierarchy.
 * @run main CannotProceedExceptionTest
 */


public class CannotProceedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.CannotProceedException e = new javax.naming.CannotProceedException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        check(e.getRemainingNewName() == null, "remainingNewName null");
        check(e.getAltName() == null, "altName null");
        check(e.getAltNameCtx() == null, "altNameCtx null");
        check(e.getEnvironment() == null, "environment null");
        javax.naming.CannotProceedException e0 = new javax.naming.CannotProceedException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
