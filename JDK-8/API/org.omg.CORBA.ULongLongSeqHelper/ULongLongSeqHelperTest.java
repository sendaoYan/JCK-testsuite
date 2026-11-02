/*
 * @test
 * @summary org.omg.CORBA.ULongLongSeqHelper conformance: helper static id/type/narrow.
 * @run main ULongLongSeqHelperTest
 */

public class ULongLongSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ULongLongSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ULongLongSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
