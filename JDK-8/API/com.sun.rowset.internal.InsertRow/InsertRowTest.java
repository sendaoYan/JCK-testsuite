/*
 * @test
 * @summary com.sun.rowset.internal.InsertRow conformance: behavior of constructors, accessors and value semantics.
 * @run main InsertRowTest
 */

public class InsertRowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.rowset.internal.InsertRow r = new com.sun.rowset.internal.InsertRow(3);
        check(r instanceof com.sun.rowset.internal.BaseRow, "extends BaseRow");
        r.setColumnObject(1, "x");
        check("x".equals(r.getColumnObject(1)), "set/getColumnObject");
        r.initInsertRow();
        check(r.getOrigRow() != null, "getOrigRow");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
