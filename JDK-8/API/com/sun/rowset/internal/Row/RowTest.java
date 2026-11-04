/*
 * @test
 * @summary com.sun.rowset.internal.Row conformance: behavior of constructors, accessors and value semantics.
 * @run main RowTest
 */

public class RowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.rowset.internal.Row r = new com.sun.rowset.internal.Row(3);
        check(r instanceof com.sun.rowset.internal.BaseRow, "extends BaseRow");
        r.setColumnObject(1, "a");
        check("a".equals(r.getColumnObject(1)), "set/getColumnObject");
        check(!r.getDeleted(), "initially not deleted");
        r.setDeleted();
        check(r.getDeleted(), "setDeleted");
        r.clearDeleted();
        check(!r.getDeleted(), "clearDeleted");
        check(!r.getInserted(), "initially not inserted");
        r.setInserted();
        check(r.getInserted(), "setInserted");
        r.clearInserted();
        check(!r.getInserted(), "clearInserted");
        check(!r.getUpdated(), "initially not updated");
        r.setUpdated();
        check(r.getUpdated(), "setUpdated");
        r.clearUpdated();
        check(!r.getUpdated(), "clearUpdated");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
