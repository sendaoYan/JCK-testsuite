/*
 * @test
 * @summary java.sql.DataTruncation conformance: index/parameter/read/dataSize/transferSize.
 * @run main DataTruncationTest
 */


public class DataTruncationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.DataTruncation e = new java.sql.DataTruncation(3, true, true, 10, 4);
        check(e instanceof java.sql.SQLWarning, "is SQLWarning");
        check(e.getIndex() == 3, "getIndex");
        check(e.getParameter() && e.getRead(), "getParameter/getRead");
        check(e.getDataSize() == 10, "getDataSize");
        check(e.getTransferSize() == 4, "getTransferSize");
        check("Data truncation".equals(e.getMessage()), "getMessage");
        java.sql.DataTruncation e2 = new java.sql.DataTruncation(0, false, false, 1, 2, new java.lang.Throwable("truncated"));
        check(e2.getCause() != null, "cause set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
