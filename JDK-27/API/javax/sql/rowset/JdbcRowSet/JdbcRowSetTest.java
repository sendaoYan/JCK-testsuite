/*
 * @test
 * @summary javax.sql.rowset.JdbcRowSet conformance: white-box structure + black-box behavior.
 * @run main JdbcRowSetTest
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.RowSet;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

public class JdbcRowSetTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.JdbcRowSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSet"), "superinterface 0");
        check(ifs[1].getName().equals("javax.sql.rowset.Joinable"), "superinterface 1");
        check(c.getDeclaredMethods().length == 8, "declared method count 8");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetFactory f = RowSetProvider.newFactory();
        JdbcRowSet jrs = f.createJdbcRowSet();
        check(jrs != null, "createJdbcRowSet non-null");
        check(jrs instanceof JdbcRowSet, "instanceof JdbcRowSet");
        check(jrs instanceof javax.sql.rowset.Joinable, "instanceof Joinable");
        check(jrs instanceof RowSet, "instanceof RowSet");
        check(jrs instanceof ResultSet, "instanceof ResultSet");
        check(jrs.getClass().getName().equals("com.sun.rowset.JdbcRowSetImpl"), "impl class name");

        // connection descriptor getters/setters (no connect)
        check(jrs.getUrl() == null, "getUrl default null");
        jrs.setUrl("jdbc:test:db");
        check("jdbc:test:db".equals(jrs.getUrl()), "setUrl/getUrl");
        check(jrs.getCommand() == null, "getCommand default null");
        jrs.setCommand("SELECT * FROM T");
        check("SELECT * FROM T".equals(jrs.getCommand()), "setCommand/getCommand");
        check(jrs.getUsername() == null, "getUsername default null");
        jrs.setUsername("user");
        check("user".equals(jrs.getUsername()), "setUsername/getUsername");
        check(jrs.getPassword() == null, "getPassword default null");
        jrs.setPassword("secret");
        check("secret".equals(jrs.getPassword()), "setPassword/getPassword");

        // result set characteristics (getType is safe without a connection)
        check(jrs.getType() == ResultSet.TYPE_SCROLL_INSENSITIVE, "getType default scroll-insensitive");
        jrs.setType(ResultSet.TYPE_FORWARD_ONLY);
        check(jrs.getType() == ResultSet.TYPE_FORWARD_ONLY, "setType/getType");

        // show-deleted roundtrip
        check(!jrs.getShowDeleted(), "getShowDeleted default false");
        jrs.setShowDeleted(true);
        check(jrs.getShowDeleted(), "setShowDeleted/getShowDeleted");

        // transaction isolation descriptor (does not connect)
        check(jrs.getTransactionIsolation() == java.sql.Connection.TRANSACTION_READ_COMMITTED,
                "getTransactionIsolation default read-committed");
        jrs.setTransactionIsolation(java.sql.Connection.TRANSACTION_SERIALIZABLE);
        check(jrs.getTransactionIsolation() == java.sql.Connection.TRANSACTION_SERIALIZABLE,
                "setTransactionIsolation/getTransactionIsolation");

        // warnings are null until a real operation has occurred
        check(jrs.getRowSetWarnings() == null, "getRowSetWarnings null on unconnected rowset");

        // read-only roundtrip (JdbcRowSetImpl constructor sets read-only true)
        check(jrs.isReadOnly(), "isReadOnly default true");
        jrs.setReadOnly(false);
        check(!jrs.isReadOnly(), "setReadOnly(false)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
