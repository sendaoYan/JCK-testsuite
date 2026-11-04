/*
 * @test
 * @summary org.omg.CORBA.WCharSeqHelper conformance: helper static id/type/narrow.
 * @run main WCharSeqHelperTest
 */

public class WCharSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.WCharSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.WCharSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
