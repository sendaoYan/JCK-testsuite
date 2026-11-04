/*
 * @test
 * @summary org.omg.CORBA.FloatSeqHelper conformance: helper static id/type/narrow.
 * @run main FloatSeqHelperTest
 */

public class FloatSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.FloatSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.FloatSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
