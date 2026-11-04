/*
 * @test
 * @summary javax.sql.RowSetInternal conformance: white-box structure + black-box behavior.
 * @run main RowSetInternalTest
 */
public class RowSetInternalTest {
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
        Class<?> c = Class.forName("javax.sql.RowSetInternal");
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
        javax.sql.RowSetInternal r = new javax.sql.RowSetInternal() {
            public Object[] getParams() { return new Object[0]; }
            public java.sql.Connection getConnection() { return null; }
            public void setMetaData(javax.sql.RowSetMetaData md) {}
            public java.sql.ResultSet getOriginal() { return null; }
            public java.sql.ResultSet getOriginalRow() { return null; }
        };
        check(r.getParams().length == 0, "getParams");
        check(r.getConnection() == null, "getConnection");
        check(r.getOriginal() == null && r.getOriginalRow() == null, "getOriginal/getOriginalRow");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
