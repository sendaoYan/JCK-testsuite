/*
 * @test
 * @summary java.sql.SQLSyntaxErrorException conformance: white-box structure + black-box behavior.
 * @run main SQLSyntaxErrorExceptionTest
 */
public class SQLSyntaxErrorExceptionTest {
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
        Class<?> c = Class.forName("java.sql.SQLSyntaxErrorException");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.sql.SQLNonTransientException"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 0, "declared method count 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.SQLSyntaxErrorException e0 = new java.sql.SQLSyntaxErrorException();
        check(e0.getMessage() == null && e0.getSQLState() == null && e0.getErrorCode() == 0, "no-arg");
        RuntimeException cause = new RuntimeException("c");
        java.sql.SQLSyntaxErrorException e1 = new java.sql.SQLSyntaxErrorException("m", "42S01", 99, cause);
        check("m".equals(e1.getMessage()), "message");
        check("42S01".equals(e1.getSQLState()), "SQLState");
        check(e1.getErrorCode() == 99, "errorCode");
        check(e1.getCause() == cause, "cause");
        java.sql.SQLSyntaxErrorException e2 = new java.sql.SQLSyntaxErrorException("only");
        check("only".equals(e2.getMessage()) && e2.getSQLState() == null && e2.getErrorCode() == 0, "message-only");
        check(e1 instanceof java.sql.SQLException, "is SQLException");
        check(e1 instanceof java.sql.SQLNonTransientException, "is SQLNonTransientException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
