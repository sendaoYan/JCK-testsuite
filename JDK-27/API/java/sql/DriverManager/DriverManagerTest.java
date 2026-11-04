/*
 * @test
 * @summary java.sql.DriverManager conformance: white-box structure + black-box behavior.
 * @run main DriverManagerTest
 */
public class DriverManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.DriverManager");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 21, "declared method count 21");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.util.Enumeration<java.sql.Driver> e = java.sql.DriverManager.getDrivers();
        check(e != null, "getDrivers non-null");
        int n = 0;
        while (e.hasMoreElements()) { e.nextElement(); n++; }
        check(java.sql.DriverManager.drivers().count() == n, "drivers() stream matches getDrivers() count");
        int saved = java.sql.DriverManager.getLoginTimeout();
        try {
            java.sql.DriverManager.setLoginTimeout(7);
            check(java.sql.DriverManager.getLoginTimeout() == 7, "login timeout set");
        } finally {
            java.sql.DriverManager.setLoginTimeout(saved);
        }
        check(java.sql.DriverManager.getLoginTimeout() == saved, "login timeout restored");
        java.sql.Driver my = new java.sql.Driver() {
            public java.sql.Connection connect(String url, java.util.Properties info) { return null; }
            public boolean acceptsURL(String url) { return false; }
            public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info) { return new java.sql.DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getLogger("jck"); }
        };
        java.sql.DriverManager.registerDriver(my);
        boolean found = false;
        java.util.Enumeration<java.sql.Driver> e2 = java.sql.DriverManager.getDrivers();
        while (e2.hasMoreElements()) { if (e2.nextElement() == my) found = true; }
        check(found, "registered driver enumerable");
        java.sql.DriverManager.deregisterDriver(my);
        boolean gone = true;
        java.util.Enumeration<java.sql.Driver> e3 = java.sql.DriverManager.getDrivers();
        while (e3.hasMoreElements()) { if (e3.nextElement() == my) gone = false; }
        check(gone, "deregistered driver gone");
        java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.StringWriter());
        java.sql.DriverManager.setLogWriter(pw);
        check(java.sql.DriverManager.getLogWriter() == pw, "log writer set");
        java.sql.DriverManager.setLogWriter(null);
        check(java.sql.DriverManager.getLogWriter() == null, "log writer null");
        java.sql.DriverManager.println("hello");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
