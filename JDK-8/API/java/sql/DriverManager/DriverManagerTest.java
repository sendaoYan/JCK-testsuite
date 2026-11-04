/*
 * @test
 * @summary java.sql.DriverManager API conformance: full contract coverage.
 *          loginTimeout set/get round-trip (incl. no-validation negative value),
 *          logWriter/logStream set/get round-trip, println no-throw, registerDriver
 *          (null -> NPE, appears in getDrivers), registerDriver(driver,DriverAction)
 *          + deregisterDriver triggers DriverAction.deregister(), deregisterDriver
 *          (null -> no-op), getConnection(null) -> SQLException("url cannot be null",
 *          "08001"), getConnection with no driver -> SQLException, getConnection via
 *          a registered in-memory fake Driver (no real DB) returns the fake
 *          Connection and passes user/password Properties, getDriver matching and
 *          "No suitable driver" SQLException. Uses a dynamic-proxy Connection and an
 *          anonymous Driver implementation: never loads a real JDBC driver or opens
 *          a real connection. Restores global DriverManager state.
 * @run main DriverManagerTest
 */

import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverAction;
import java.sql.DriverPropertyInfo;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class DriverManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Connection fakeConnection() {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new java.lang.reflect.InvocationHandler() {
                    public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                        return null;
                    }
                });
    }

    static Driver fakeDriver(final String prefix, final Connection conn, final Properties[] capture) {
        return new Driver() {
            public Connection connect(String url, Properties info) {
                if (capture != null) capture[0] = info;
                return conn;
            }
            public boolean acceptsURL(String url) {
                return url != null && url.startsWith(prefix);
            }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
                return new DriverPropertyInfo[0];
            }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public Logger getParentLogger() { return null; }
        };
    }

    static boolean driverRegistered(Driver d) {
        Enumeration<Driver> e = java.sql.DriverManager.getDrivers();
        while (e.hasMoreElements()) {
            if (e.nextElement() == d) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        int savedTimeout = java.sql.DriverManager.getLoginTimeout();
        PrintWriter savedWriter = java.sql.DriverManager.getLogWriter();
        PrintStream savedStream = java.sql.DriverManager.getLogStream();
        try {
            testLoginTimeout();
            testLogWriterStream();
            testPrintln();
            testRegisterDeregister();
            testRegisterWithDriverAction();
            testGetConnection();
            testGetDriver();
        } finally {
            java.sql.DriverManager.setLoginTimeout(savedTimeout);
            java.sql.DriverManager.setLogWriter(savedWriter);
            if (savedStream != null) {
                java.sql.DriverManager.setLogStream(savedStream);
            }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testLoginTimeout() {
        java.sql.DriverManager.setLoginTimeout(0);
        check(java.sql.DriverManager.getLoginTimeout() == 0, "setLoginTimeout(0)");
        java.sql.DriverManager.setLoginTimeout(42);
        check(java.sql.DriverManager.getLoginTimeout() == 42, "setLoginTimeout(42)");
        java.sql.DriverManager.setLoginTimeout(-1);
        check(java.sql.DriverManager.getLoginTimeout() == -1, "setLoginTimeout(-1) no validation");
        java.sql.DriverManager.setLoginTimeout(Integer.MAX_VALUE);
        check(java.sql.DriverManager.getLoginTimeout() == Integer.MAX_VALUE, "setLoginTimeout(MAX_VALUE)");
    }

    static void testLogWriterStream() {
        java.sql.DriverManager.setLogWriter(null);
        check(java.sql.DriverManager.getLogWriter() == null, "setLogWriter(null)");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        java.sql.DriverManager.setLogWriter(pw);
        check(java.sql.DriverManager.getLogWriter() == pw, "setLogWriter(PrintWriter)");

        java.sql.DriverManager.setLogStream(null);
        check(java.sql.DriverManager.getLogStream() == null, "setLogStream(null)");
        check(java.sql.DriverManager.getLogWriter() == null, "setLogStream(null) clears logWriter");

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        java.sql.DriverManager.setLogStream(ps);
        check(java.sql.DriverManager.getLogStream() == ps, "setLogStream(PrintStream)");
        check(java.sql.DriverManager.getLogWriter() != null, "setLogStream sets logWriter");
    }

    static void testPrintln() {
        // println is a no-throw operation; with a null writer it is a no-op.
        java.sql.DriverManager.setLogWriter(null);
        java.sql.DriverManager.println("trace message");
        check(true, "println no-throw");
    }

    static void testRegisterDeregister() throws Exception {
        final Connection conn = fakeConnection();
        final Driver d = fakeDriver("jdbc:dmtest:", conn, null);
        java.sql.DriverManager.registerDriver(d);
        check(driverRegistered(d), "registerDriver appears in getDrivers");
        java.sql.DriverManager.deregisterDriver(d);
        check(!driverRegistered(d), "deregisterDriver removes driver");

        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { java.sql.DriverManager.registerDriver(null); }
        }, "registerDriver(null)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { java.sql.DriverManager.registerDriver(null, null); }
        }, "registerDriver(null, null)");

        // deregisterDriver(null) is a documented no-op (does not throw).
        try {
            java.sql.DriverManager.deregisterDriver(null);
            passed++;
        } catch (Throwable t) {
            throw new AssertionError("deregisterDriver(null) should be a no-op", t);
        }
    }

    static void testRegisterWithDriverAction() throws Exception {
        final boolean[] deregistered = new boolean[]{false};
        final Connection conn = fakeConnection();
        final Driver d = fakeDriver("jdbc:dmact:", conn, null);
        DriverAction action = new DriverAction() {
            public void deregister() { deregistered[0] = true; }
        };
        java.sql.DriverManager.registerDriver(d, action);
        check(driverRegistered(d), "registerDriver(driver,action) registered");
        check(!deregistered[0], "action not called on register");
        java.sql.DriverManager.deregisterDriver(d);
        check(deregistered[0], "deregisterDriver invokes DriverAction.deregister()");
    }

    static void testGetConnection() throws Exception {
        // null url -> SQLException "The url cannot be null" with SQLState 08001.
        checkThrows(java.sql.SQLException.class, new ThrowingRunnable() {
            public void run() throws Exception { java.sql.DriverManager.getConnection((String) null); }
        }, "getConnection(null)");
        try {
            java.sql.DriverManager.getConnection((String) null);
        } catch (java.sql.SQLException e) {
            check("08001".equals(e.getSQLState()), "getConnection(null) SQLState 08001");
            check(e.getMessage().contains("null"), "getConnection(null) message mentions null");
        }

        // No suitable driver for an unknown URL.
        try {
            java.sql.DriverManager.getConnection("jdbc:dmtest:nosuch:x");
            throw new AssertionError("expected SQLException");
        } catch (java.sql.SQLException e) {
            check("08001".equals(e.getSQLState()), "no-driver SQLState 08001");
            check(e.getMessage().contains("No suitable driver"), "no-driver message");
        }

        // Registered in-memory fake driver returns the fake connection + user/password props.
        final Connection conn = fakeConnection();
        final Properties[] captured = new Properties[1];
        final Driver d = fakeDriver("jdbc:dmtest:", conn, captured);
        java.sql.DriverManager.registerDriver(d);
        try {
            Connection got = java.sql.DriverManager.getConnection("jdbc:dmtest:db");
            check(got == conn, "getConnection returns fake driver's connection");
            got = java.sql.DriverManager.getConnection("jdbc:dmtest:db", "alice", "s3cret");
            check(got == conn, "getConnection(url,user,password) returns connection");
            check(captured[0] != null, "connect received Properties");
            check("alice".equals(captured[0].getProperty("user")), "Properties user");
            check("s3cret".equals(captured[0].getProperty("password")), "Properties password");
        } finally {
            java.sql.DriverManager.deregisterDriver(d);
        }
    }

    static void testGetDriver() throws Exception {
        final Connection conn = fakeConnection();
        final Driver d = fakeDriver("jdbc:dmtest:", conn, null);
        java.sql.DriverManager.registerDriver(d);
        try {
            Driver got = java.sql.DriverManager.getDriver("jdbc:dmtest:x");
            check(got == d, "getDriver returns matching registered driver");
        } finally {
            java.sql.DriverManager.deregisterDriver(d);
        }

        try {
            java.sql.DriverManager.getDriver("jdbc:dmtest:nodriver:x");
            throw new AssertionError("expected SQLException");
        } catch (java.sql.SQLException e) {
            check("08001".equals(e.getSQLState()), "getDriver no-match SQLState 08001");
            check(e.getMessage().contains("No suitable driver"), "getDriver no-match message");
        }
    }
}
