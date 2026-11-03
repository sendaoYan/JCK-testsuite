/*
 * @test
 * @summary javax.sql.ConnectionEventListener conformance: white-box structure + black-box behavior.
 * @run main ConnectionEventListenerTest
 */
public class ConnectionEventListenerTest {
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
        Class<?> c = Class.forName("javax.sql.ConnectionEventListener");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.util.EventListener"), "superinterface 0");
        check(c.getDeclaredMethods().length == 2, "declared method count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] n = {0};
        javax.sql.ConnectionEventListener l = new javax.sql.ConnectionEventListener() {
            public void connectionClosed(javax.sql.ConnectionEvent e) { n[0]++; }
            public void connectionErrorOccurred(javax.sql.ConnectionEvent e) { n[0]++; }
        };
        javax.sql.PooledConnection pc = new javax.sql.PooledConnection() {
            public java.sql.Connection getConnection() { return null; }
            public void close() {}
            public void addConnectionEventListener(javax.sql.ConnectionEventListener x) {}
            public void removeConnectionEventListener(javax.sql.ConnectionEventListener x) {}
            public void addStatementEventListener(javax.sql.StatementEventListener x) {}
            public void removeStatementEventListener(javax.sql.StatementEventListener x) {}
        };
        javax.sql.ConnectionEvent ev = new javax.sql.ConnectionEvent(pc);
        l.connectionClosed(ev); l.connectionErrorOccurred(ev);
        check(n[0] == 2, "listener methods called");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
