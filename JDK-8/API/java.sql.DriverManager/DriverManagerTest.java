/*
 * @test
 * @summary java.sql.DriverManager conformance: loginTimeout/log/drivers.
 * @run main DriverManagerTest
 */


public class DriverManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        int t = java.sql.DriverManager.getLoginTimeout();
        java.sql.DriverManager.setLoginTimeout(0);
        check(java.sql.DriverManager.getLoginTimeout() == 0, "setLoginTimeout(0)");
        java.sql.DriverManager.setLogWriter(null);
        check(java.sql.DriverManager.getLogWriter() == null, "getLogWriter null");
        check(java.sql.DriverManager.getDrivers() != null, "getDrivers non-null");
        try {
            java.sql.DriverManager.getConnection("jdbc:nosuchdriver:foo");
            throw new AssertionError("expected SQLException");
        } catch (java.sql.SQLException expected) {
            check(expected.getMessage().contains("No suitable driver"), "no suitable driver");
        }        System.out.println("PASS (" + passed + " assertions)");
    }
}
