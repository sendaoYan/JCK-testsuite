/*
 * @test
 * @summary javax.sql.rowset.BaseRowSet conformance: white-box structure + black-box behavior.
 * @run main BaseRowSetTest
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BaseRowSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowableRunnable { void run() throws Exception; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowableRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " threw " + t.getClass().getName() + ": " + t, t);
        }
        throw new AssertionError(msg + " did not throw " + expected.getName());
    }

    static javax.sql.rowset.BaseRowSet newBaseRowSet() {
        return new javax.sql.rowset.BaseRowSet() {};
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.BaseRowSet");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 2, "implements Serializable and Cloneable");
        check(c.getDeclaredMethods().length == 130, "declared method count 130");
        check(javax.sql.rowset.BaseRowSet.UNICODE_STREAM_PARAM == 0, "UNICODE_STREAM_PARAM == 0");
        check(javax.sql.rowset.BaseRowSet.BINARY_STREAM_PARAM == 1, "BINARY_STREAM_PARAM == 1");
        check(javax.sql.rowset.BaseRowSet.ASCII_STREAM_PARAM == 2, "ASCII_STREAM_PARAM == 2");
        check(c.getDeclaredConstructors().length == 1, "one declared constructor");
        check(c.getDeclaredConstructors()[0].getParameterCount() == 0, "no-arg constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.sql.rowset.BaseRowSet b = newBaseRowSet();
        b.getParams(); // initializes the parameter store required by setCommand/setXxx

        // read-only / show-deleted / credentials roundtrip
        check(!b.isReadOnly(), "isReadOnly default false");
        b.setReadOnly(true);
        check(b.isReadOnly(), "setReadOnly/isReadOnly");
        check(!b.getShowDeleted(), "getShowDeleted default false");
        b.setShowDeleted(true);
        check(b.getShowDeleted(), "setShowDeleted/getShowDeleted");
        check(b.getUsername() == null, "getUsername default null");
        b.setUsername("u");
        check("u".equals(b.getUsername()), "setUsername/getUsername");
        b.setUsername(null);
        check(b.getUsername() == null, "setUsername(null)");
        b.setPassword("p");
        check("p".equals(b.getPassword()), "setPassword/getPassword");

        // command
        check(b.getCommand() == null, "getCommand default null");
        b.setCommand("SELECT 1");
        check("SELECT 1".equals(b.getCommand()), "setCommand/getCommand");

        // url / dataSourceName mutual exclusion
        check(b.getUrl() == null, "getUrl default null");
        b.setUrl("jdbc:test:db");
        check("jdbc:test:db".equals(b.getUrl()), "setUrl/getUrl");
        checkThrows(SQLException.class, () -> b.setUrl(""), "setUrl empty string throws SQLException");
        b.setDataSourceName("jdbc/myDS");
        check("jdbc/myDS".equals(b.getDataSourceName()), "setDataSourceName/getDataSourceName");
        check(b.getUrl() == null, "setDataSourceName clears url");
        b.setUrl("jdbc:again");
        check(b.getDataSourceName() == null, "setUrl clears dataSourceName");
        checkThrows(SQLException.class, () -> b.setDataSourceName(""), "setDataSourceName empty string throws SQLException");

        // type / concurrency
        check(b.getType() == ResultSet.TYPE_SCROLL_INSENSITIVE, "getType default TYPE_SCROLL_INSENSITIVE");
        b.setType(ResultSet.TYPE_FORWARD_ONLY);
        check(b.getType() == ResultSet.TYPE_FORWARD_ONLY, "setType/getType");
        checkThrows(SQLException.class, () -> b.setType(9999), "setType invalid throws SQLException");
        check(b.getConcurrency() == ResultSet.CONCUR_UPDATABLE, "getConcurrency default CONCUR_UPDATABLE");
        b.setConcurrency(ResultSet.CONCUR_READ_ONLY);
        check(b.getConcurrency() == ResultSet.CONCUR_READ_ONLY, "setConcurrency/getConcurrency");
        checkThrows(SQLException.class, () -> b.setConcurrency(9999), "setConcurrency invalid throws SQLException");

        // transaction isolation
        check(b.getTransactionIsolation() == Connection.TRANSACTION_NONE, "getTransactionIsolation default TRANSACTION_NONE");
        b.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        check(b.getTransactionIsolation() == Connection.TRANSACTION_READ_COMMITTED, "setTransactionIsolation/getTransactionIsolation");
        checkThrows(SQLException.class, () -> b.setTransactionIsolation(9999), "setTransactionIsolation invalid throws SQLException");

        // type map
        check(b.getTypeMap() == null, "getTypeMap default null");
        Map<String, Class<?>> tm = new HashMap<>();
        tm.put("MYTYPE", String.class);
        b.setTypeMap(tm);
        check(b.getTypeMap() == tm, "setTypeMap/getTypeMap same map");

        // max field size / max rows / query timeout
        check(b.getMaxFieldSize() == 0, "getMaxFieldSize default 0");
        b.setMaxFieldSize(100);
        check(b.getMaxFieldSize() == 100, "setMaxFieldSize/getMaxFieldSize");
        checkThrows(SQLException.class, () -> b.setMaxFieldSize(-1), "setMaxFieldSize negative throws SQLException");
        check(b.getMaxRows() == 0, "getMaxRows default 0");
        b.setMaxRows(50);
        check(b.getMaxRows() == 50, "setMaxRows/getMaxRows");
        checkThrows(SQLException.class, () -> b.setMaxRows(-1), "setMaxRows negative throws SQLException");
        check(b.getQueryTimeout() == 0, "getQueryTimeout default 0");
        b.setQueryTimeout(30);
        check(b.getQueryTimeout() == 30, "setQueryTimeout/getQueryTimeout");
        checkThrows(SQLException.class, () -> b.setQueryTimeout(-5), "setQueryTimeout negative throws SQLException");

        // escape processing
        check(b.getEscapeProcessing(), "getEscapeProcessing default true");
        b.setEscapeProcessing(false);
        check(!b.getEscapeProcessing(), "setEscapeProcessing/getEscapeProcessing");

        // fetch direction / size
        b.setType(ResultSet.TYPE_SCROLL_INSENSITIVE); // fetch direction other than FORWARD requires scrollable
        check(b.getFetchDirection() == ResultSet.FETCH_FORWARD, "getFetchDirection default FETCH_FORWARD");
        b.setFetchDirection(ResultSet.FETCH_REVERSE);
        check(b.getFetchDirection() == ResultSet.FETCH_REVERSE, "setFetchDirection/getFetchDirection");
        checkThrows(SQLException.class, () -> b.setFetchDirection(9999), "setFetchDirection invalid throws SQLException");
        check(b.getFetchSize() == 0, "getFetchSize default 0");
        b.setFetchSize(20);
        check(b.getFetchSize() == 20, "setFetchSize/getFetchSize");
        checkThrows(SQLException.class, () -> b.setFetchSize(-1), "setFetchSize negative throws SQLException");

        // parameters: set params and read them back
        b.setString(1, "x");
        b.setInt(2, 42);
        Object[] p = b.getParams();
        check(p.length == 2, "getParams length 2");
        check("x".equals(p[0]), "param 0 string");
        check(Integer.valueOf(42).equals(p[1]), "param 1 integer");
        b.clearParameters();
        check(b.getParams().length == 0, "clearParameters empties params");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
