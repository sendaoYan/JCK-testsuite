/*
 * @test
 * @summary org.omg.CORBA.AnySeqHelper conformance: helper static id/type/narrow.
 * @run main AnySeqHelperTest
 */

public class AnySeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.AnySeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.AnySeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
