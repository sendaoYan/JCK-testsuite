/*
 * @test
 * @summary java.sql.DriverManager conformance: driver enumeration and login/log writer state
 * @run main DriverManagerTest
 */

public class DriverManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.util.Enumeration<java.sql.Driver> e = java.sql.DriverManager.getDrivers();
        check(e != null, "getDrivers non-null");
        int n = 0;
        while (e.hasMoreElements()) { e.nextElement(); n++; }
        long sn = java.sql.DriverManager.drivers().count();
        check(sn == n, "drivers() stream matches getDrivers() count");
        java.sql.DriverManager.setLoginTimeout(7);
        check(java.sql.DriverManager.getLoginTimeout() == 7, "login timeout set");
        java.sql.DriverManager.setLoginTimeout(0);
        check(java.sql.DriverManager.getLoginTimeout() == 0, "login timeout reset");
        java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.StringWriter());
        java.sql.DriverManager.setLogWriter(pw);
        check(java.sql.DriverManager.getLogWriter() == pw, "log writer");
        java.sql.DriverManager.setLogWriter(null);
        check(java.sql.DriverManager.getLogWriter() == null, "log writer null");
        java.sql.DriverManager.println("hello");        System.out.println("PASS (" + passed + " assertions)");
    }
}
