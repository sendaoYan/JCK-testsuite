/*
 * @test
 * @summary javax.sql.XAConnectionBuilder conformance: white-box structure + black-box behavior.
 * @run main XAConnectionBuilderTest
 */
public class XAConnectionBuilderTest {
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
        Class<?> c = Class.forName("javax.sql.XAConnectionBuilder");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 5, "declared method count 5");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final javax.sql.XAConnection xa = new javax.sql.XAConnection() {
            public java.sql.Connection getConnection() { return null; }
            public void close() {}
            public void addConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void removeConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void addStatementEventListener(javax.sql.StatementEventListener l) {}
            public void removeStatementEventListener(javax.sql.StatementEventListener l) {}
            public javax.transaction.xa.XAResource getXAResource() { return null; }
        };
        javax.sql.XAConnectionBuilder b = new javax.sql.XAConnectionBuilder() {
            public javax.sql.XAConnectionBuilder user(String u) { return this; }
            public javax.sql.XAConnectionBuilder password(String p) { return this; }
            public javax.sql.XAConnectionBuilder shardingKey(java.sql.ShardingKey k) { return this; }
            public javax.sql.XAConnectionBuilder superShardingKey(java.sql.ShardingKey k) { return this; }
            public javax.sql.XAConnection build() { return xa; }
        };
        check(b.user("u").password("p").build() == xa, "fluent build");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
