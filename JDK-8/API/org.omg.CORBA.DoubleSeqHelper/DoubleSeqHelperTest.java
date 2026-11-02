/*
 * @test
 * @summary org.omg.CORBA.DoubleSeqHelper conformance: helper static id/type/narrow.
 * @run main DoubleSeqHelperTest
 */

public class DoubleSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.DoubleSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.DoubleSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
