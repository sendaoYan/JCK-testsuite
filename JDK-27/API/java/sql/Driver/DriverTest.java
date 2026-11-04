/*
 * @test
 * @summary java.sql.Driver conformance: white-box structure + black-box behavior.
 * @run main DriverTest
 */
public class DriverTest {
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
        Class<?> c = Class.forName("java.sql.Driver");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 7, "declared method count 7");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.Driver d = new java.sql.Driver() {
            public java.sql.Connection connect(String url, java.util.Properties info) { return null; }
            public boolean acceptsURL(String url) { return false; }
            public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info) { return new java.sql.DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 2; }
            public boolean jdbcCompliant() { return false; }
            public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getLogger("jck"); }
        };
        check(d.connect("jdbc:x", new java.util.Properties()) == null, "connect returns null");
        check(!d.acceptsURL("jdbc:x"), "acceptsURL false");
        check(d.getPropertyInfo("jdbc:x", new java.util.Properties()).length == 0, "getPropertyInfo empty");
        check(d.getMajorVersion() == 1, "getMajorVersion");
        check(d.getMinorVersion() == 2, "getMinorVersion");
        check(!d.jdbcCompliant(), "jdbcCompliant");
        check(d.getParentLogger() != null, "getParentLogger");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
