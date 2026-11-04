/*
 * @test
 * @summary javax.sql.PooledConnectionBuilder conformance: white-box structure + black-box behavior.
 * @run main PooledConnectionBuilderTest
 */
public class PooledConnectionBuilderTest {
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
        Class<?> c = Class.forName("javax.sql.PooledConnectionBuilder");
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
        final javax.sql.PooledConnection pc = new javax.sql.PooledConnection() {
            public java.sql.Connection getConnection() { return null; }
            public void close() {}
            public void addConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void removeConnectionEventListener(javax.sql.ConnectionEventListener l) {}
            public void addStatementEventListener(javax.sql.StatementEventListener l) {}
            public void removeStatementEventListener(javax.sql.StatementEventListener l) {}
        };
        javax.sql.PooledConnectionBuilder b = new javax.sql.PooledConnectionBuilder() {
            public javax.sql.PooledConnectionBuilder user(String u) { return this; }
            public javax.sql.PooledConnectionBuilder password(String p) { return this; }
            public javax.sql.PooledConnectionBuilder shardingKey(java.sql.ShardingKey k) { return this; }
            public javax.sql.PooledConnectionBuilder superShardingKey(java.sql.ShardingKey k) { return this; }
            public javax.sql.PooledConnection build() { return pc; }
        };
        check(b.user("u").password("p").build() == pc, "fluent build");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
