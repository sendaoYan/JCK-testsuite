/*
 * @test
 * @summary org.omg.CORBA.UShortSeqHelper conformance: helper static id/type/narrow.
 * @run main UShortSeqHelperTest
 */

public class UShortSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UShortSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.UShortSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
