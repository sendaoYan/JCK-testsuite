/*
 * @test
 * @summary org.omg.CORBA.CharSeqHelper conformance: helper static id/type/narrow.
 * @run main CharSeqHelperTest
 */

public class CharSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.CharSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.CharSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
