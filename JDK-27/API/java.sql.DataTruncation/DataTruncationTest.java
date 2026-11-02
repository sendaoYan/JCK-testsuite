/*
 * @test
 * @summary java.sql.DataTruncation conformance: constructor and truncation metadata
 * @run main DataTruncationTest
 */

public class DataTruncationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.DataTruncation d = new java.sql.DataTruncation(2, true, false, 10, 5);
        check(d.getIndex() == 2, "getIndex");
        check(d.getParameter() == true, "getParameter");
        check(d.getRead() == false, "getRead");
        check(d.getDataSize() == 10, "getDataSize");
        check(d.getTransferSize() == 5, "getTransferSize");
        check("Data truncation".equals(d.getMessage()), "message");
        check("22001".equals(d.getSQLState()), "SQLState");
        check(d.getErrorCode() == 0, "errorCode");
        RuntimeException cause = new RuntimeException("c");
        java.sql.DataTruncation d2 = new java.sql.DataTruncation(1, false, true, 3, 4, cause);
        check(d2.getCause() == cause, "cause");
        check(d2.getTransferSize() == 4 && d2.getRead() == true, "second ctor fields");
        check(d instanceof java.sql.SQLWarning, "extends SQLWarning");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
