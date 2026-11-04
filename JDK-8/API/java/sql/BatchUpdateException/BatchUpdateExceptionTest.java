/*
 * @test
 * @summary java.sql.BatchUpdateException conformance: message and update counts.
 * @run main BatchUpdateExceptionTest
 */


public class BatchUpdateExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.BatchUpdateException e = new java.sql.BatchUpdateException("boom", new int[]{1, 2});
        check("boom".equals(e.getMessage()), "getMessage(String,int[])");
        check(e instanceof java.sql.SQLException, "is SQLException");
        check(e.getUpdateCounts().length == 2 && e.getUpdateCounts()[1] == 2, "getUpdateCounts");
        java.sql.BatchUpdateException e0 = new java.sql.BatchUpdateException();
        check(e0.getMessage() == null && e0.getUpdateCounts() == null, "no-arg defaults");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
