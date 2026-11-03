/*
 * @test
 * @summary javax.sql.XAConnection conformance: white-box structure + black-box behavior.
 * @run main XAConnectionTest
 */
public class XAConnectionTest {
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
        Class<?> c = Class.forName("javax.sql.XAConnection");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.PooledConnection"), "superinterface 0");
        check(c.getDeclaredMethods().length == 1, "declared method count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.sql.XAConnection xa = new javax.sql.XAConnection() {
            public java.sql.Connection getConnection() { return null; }
            public void close() {}
            public void addConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void removeConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void addStatementEventListener(javax.sql.StatementEventListener l) {}
            public void removeStatementEventListener(javax.sql.StatementEventListener l) {}
            public javax.transaction.xa.XAResource getXAResource() { return null; }
        };
        check(xa.getXAResource() == null, "getXAResource");
        check(xa instanceof javax.sql.PooledConnection, "is PooledConnection");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
