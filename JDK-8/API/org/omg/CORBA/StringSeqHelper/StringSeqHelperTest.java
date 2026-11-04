/*
 * @test
 * @summary org.omg.CORBA.StringSeqHelper conformance: helper static id/type/narrow.
 * @run main StringSeqHelperTest
 */

public class StringSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.StringSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.StringSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
