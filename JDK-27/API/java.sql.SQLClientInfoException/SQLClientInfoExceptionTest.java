/*
 * @test
 * @summary java.sql.SQLClientInfoException conformance: white-box structure + black-box behavior.
 * @run main SQLClientInfoExceptionTest
 */
public class SQLClientInfoExceptionTest {
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
        Class<?> c = Class.forName("java.sql.SQLClientInfoException");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.sql.SQLException"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 1, "declared method count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.util.Map<String, java.sql.ClientInfoStatus> failed = new java.util.HashMap<>();
        failed.put("user", java.sql.ClientInfoStatus.REASON_VALUE_INVALID);
        java.sql.SQLClientInfoException x = new java.sql.SQLClientInfoException("bad", "45000", 5, failed);
        java.util.Map<String, java.sql.ClientInfoStatus> got = x.getFailedProperties();
        check(got.size() == 1, "failed properties size");
        check(got.get("user") == java.sql.ClientInfoStatus.REASON_VALUE_INVALID, "failed property value");
        check("bad".equals(x.getMessage()) && "45000".equals(x.getSQLState()) && x.getErrorCode() == 5, "message/state/code");
        RuntimeException cause = new RuntimeException("c");
        java.sql.SQLClientInfoException x2 = new java.sql.SQLClientInfoException("m", failed, cause);
        check(x2.getCause() == cause && x2.getFailedProperties() == failed, "cause + map");
        check(x instanceof java.sql.SQLException, "is SQLException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
