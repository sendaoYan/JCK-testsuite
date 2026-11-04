/*
 * @test
 * @summary org.omg.CORBA.BooleanSeqHelper conformance: helper static id/type/narrow.
 * @run main BooleanSeqHelperTest
 */

public class BooleanSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.BooleanSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.BooleanSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
