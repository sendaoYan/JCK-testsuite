/*
 * @test
 * @summary javax.sql.ConnectionEvent conformance: white-box structure + black-box behavior.
 * @run main ConnectionEventTest
 */
public class ConnectionEventTest {
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
        Class<?> c = Class.forName("javax.sql.ConnectionEvent");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 1, "declared method count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.sql.PooledConnection pc = new javax.sql.PooledConnection() {
            public java.sql.Connection getConnection() { return null; }
            public void close() {}
            public void addConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void removeConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void addStatementEventListener(javax.sql.StatementEventListener l) {}
            public void removeStatementEventListener(javax.sql.StatementEventListener l) {}
        };
        javax.sql.ConnectionEvent ev = new javax.sql.ConnectionEvent(pc);
        check(ev.getSource() == pc, "getSource");
        check(ev.getSQLException() == null, "getSQLException null");
        java.sql.SQLException se = new java.sql.SQLException("boom");
        javax.sql.ConnectionEvent ev2 = new javax.sql.ConnectionEvent(pc, se);
        check(ev2.getSQLException() == se, "getSQLException set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
