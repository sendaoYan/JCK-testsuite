/*
 * @test
 * @summary org.omg.CORBA.ULongSeqHelper conformance: helper static id/type/narrow.
 * @run main ULongSeqHelperTest
 */

public class ULongSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ULongSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ULongSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
