/*
 * @test
 * @summary javax.naming.ConfigurationException conformance: message and hierarchy.
 * @run main ConfigurationExceptionTest
 */


public class ConfigurationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ConfigurationException e = new javax.naming.ConfigurationException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.ConfigurationException e0 = new javax.naming.ConfigurationException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
