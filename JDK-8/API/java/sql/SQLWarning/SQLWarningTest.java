/*
 * @test
 * @summary java.sql.SQLWarning conformance: message and hierarchy.
 * @run main SQLWarningTest
 */


public class SQLWarningTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLWarning e = new java.sql.SQLWarning("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLException, "is SQLException");
        check(e.getNextWarning() == null, "no next warning");
        java.sql.SQLWarning w = new java.sql.SQLWarning();
        e.setNextWarning(w);
        check(e.getNextWarning() == w, "setNextWarning");
        java.sql.SQLWarning e0 = new java.sql.SQLWarning();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
