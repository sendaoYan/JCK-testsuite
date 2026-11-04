/*
 * @test
 * @summary java.sql.ShardingKeyBuilder conformance: white-box structure + black-box behavior.
 * @run main ShardingKeyBuilderTest
 */
public class ShardingKeyBuilderTest {
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
        Class<?> c = Class.forName("java.sql.ShardingKeyBuilder");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 2, "declared method count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.sql.ShardingKey key = new java.sql.ShardingKey() {};
        java.sql.ShardingKeyBuilder b = new java.sql.ShardingKeyBuilder() {
            public java.sql.ShardingKeyBuilder subkey(Object o, java.sql.SQLType t) { return this; }
            public java.sql.ShardingKey build() { return key; }
        };
        check(b.subkey("k", java.sql.JDBCType.VARCHAR) == b, "subkey fluent");
        check(b.build() == key, "build");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
