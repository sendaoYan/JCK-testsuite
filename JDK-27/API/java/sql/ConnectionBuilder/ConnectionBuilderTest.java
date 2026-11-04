/*
 * @test
 * @summary java.sql.ConnectionBuilder conformance: white-box structure + black-box behavior.
 * @run main ConnectionBuilderTest
 */
public class ConnectionBuilderTest {
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
        Class<?> c = Class.forName("java.sql.ConnectionBuilder");
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
        final java.sql.Connection conn = (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(
            ConnectionBuilderTest.class.getClassLoader(), new Class[]{java.sql.Connection.class},
            (p, m, a) -> { if (m.getReturnType() == boolean.class) return false; if (m.getReturnType() == int.class) return 0; return null; });
        java.sql.ConnectionBuilder b = new java.sql.ConnectionBuilder() {
            public java.sql.ConnectionBuilder user(String u) { return this; }
            public java.sql.ConnectionBuilder password(String p) { return this; }
            public java.sql.ConnectionBuilder shardingKey(java.sql.ShardingKey k) { return this; }
            public java.sql.ConnectionBuilder superShardingKey(java.sql.ShardingKey k) { return this; }
            public java.sql.Connection build() { return conn; }
        };
        check(b.user("u").password("p").build() == conn, "fluent build");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
