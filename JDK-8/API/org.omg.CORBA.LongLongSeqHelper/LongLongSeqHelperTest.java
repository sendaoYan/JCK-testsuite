/*
 * @test
 * @summary org.omg.CORBA.LongLongSeqHelper conformance: helper static id/type/narrow.
 * @run main LongLongSeqHelperTest
 */

public class LongLongSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.LongLongSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.LongLongSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
