/*
 * @test
 * @summary org.omg.CORBA.WStringSeqHelper conformance: helper static id/type/narrow.
 * @run main WStringSeqHelperTest
 */

public class WStringSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.WStringSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.WStringSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
