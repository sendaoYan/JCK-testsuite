/*
 * @test
 * @summary java.sql.BatchUpdateException conformance: constructors and update-count arrays
 * @run main BatchUpdateExceptionTest
 */

public class BatchUpdateExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        int[] counts = {1, 2, 3};
        java.sql.BatchUpdateException e = new java.sql.BatchUpdateException("m", "st", 1, counts);
        check(java.util.Arrays.equals(e.getUpdateCounts(), counts), "getUpdateCounts");
        check("st".equals(e.getSQLState()) && e.getErrorCode() == 1, "state/code");
        long[] lcounts = {10L, 20L};
        RuntimeException cause = new RuntimeException("c");
        java.sql.BatchUpdateException e2 = new java.sql.BatchUpdateException("m", "st", 1, lcounts, cause);
        check(java.util.Arrays.equals(e2.getLargeUpdateCounts(), lcounts), "getLargeUpdateCounts");
        check(e2.getCause() == cause, "cause");
        check(new java.sql.BatchUpdateException().getUpdateCounts() == null, "no-arg update counts null");
        check(new java.sql.BatchUpdateException().getLargeUpdateCounts() == null, "no-arg large counts null");
        check(e instanceof java.sql.SQLException, "extends SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
