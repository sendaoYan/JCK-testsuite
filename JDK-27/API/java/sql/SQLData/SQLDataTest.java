/*
 * @test
 * @summary java.sql.SQLData conformance: white-box structure + black-box behavior.
 * @run main SQLDataTest
 */
public class SQLDataTest {
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
        Class<?> c = Class.forName("java.sql.SQLData");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 3, "declared method count 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final String[] written = {null};
        java.sql.SQLData d = new java.sql.SQLData() {
            public String getSQLTypeName() { return "T"; }
            public void readSQL(java.sql.SQLInput in, String type) {}
            public void writeSQL(java.sql.SQLOutput out) { written[0] = "called"; }
        };
        check("T".equals(d.getSQLTypeName()), "getSQLTypeName");
        d.writeSQL(null);
        check("called".equals(written[0]), "writeSQL called");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
