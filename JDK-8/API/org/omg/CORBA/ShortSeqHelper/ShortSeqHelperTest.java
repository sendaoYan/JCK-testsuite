/*
 * @test
 * @summary org.omg.CORBA.ShortSeqHelper conformance: helper static id/type/narrow.
 * @run main ShortSeqHelperTest
 */

public class ShortSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ShortSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ShortSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
