/*
 * @test
 * @summary javax.sql.ConnectionEvent conformance: source/sqlException.
 * @run main ConnectionEventTest
 */


public class ConnectionEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static javax.sql.PooledConnection mkPooled() {
        return new javax.sql.PooledConnection() {
            public java.sql.Connection getConnection() { return null; }
            public void close() {}
            public void addConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void removeConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void addStatementEventListener(javax.sql.StatementEventListener l) {}
            public void removeStatementEventListener(javax.sql.StatementEventListener l) {}
        };
    }
    public static void main(String[] args) throws Exception {
        javax.sql.PooledConnection pc = mkPooled();
        javax.sql.ConnectionEvent e = new javax.sql.ConnectionEvent(pc);
        check(e.getSource() == pc, "getSource");
        check(e.getSQLException() == null, "getSQLException null");
        java.sql.SQLException ex = new java.sql.SQLException("closed");
        javax.sql.ConnectionEvent e2 = new javax.sql.ConnectionEvent(pc, ex);
        check(e2.getSQLException() == ex, "getSQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
