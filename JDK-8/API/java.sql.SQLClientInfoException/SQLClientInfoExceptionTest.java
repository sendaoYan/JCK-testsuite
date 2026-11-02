/*
 * @test
 * @summary java.sql.SQLClientInfoException conformance: message and failed properties.
 * @run main SQLClientInfoExceptionTest
 */


public class SQLClientInfoExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLClientInfoException e = new java.sql.SQLClientInfoException("boom", null);
        check("boom".equals(e.getMessage()), "getMessage(String,Map)");
        check(e instanceof java.sql.SQLException, "is SQLException");
        check(e.getFailedProperties() == null, "getFailedProperties null");
        java.sql.SQLClientInfoException e0 = new java.sql.SQLClientInfoException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
