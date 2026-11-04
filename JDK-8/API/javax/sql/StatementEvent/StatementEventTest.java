/*
 * @test
 * @summary javax.sql.StatementEvent conformance: statement/sqlException.
 * @run main StatementEventTest
 */


public class StatementEventTest {
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
        javax.sql.StatementEvent e = new javax.sql.StatementEvent(pc, null);
        check(e.getSource() == pc, "getSource");
        check(e.getStatement() == null, "getStatement null");
        check(e.getSQLException() == null, "getSQLException null");
        java.sql.SQLException ex = new java.sql.SQLException("bad");
        javax.sql.StatementEvent e2 = new javax.sql.StatementEvent(pc, null, ex);
        check(e2.getSQLException() == ex, "getSQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
