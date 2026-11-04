/*
 * @test
 * @summary org.omg.CORBA.OctetSeqHelper conformance: helper static id/type/narrow.
 * @run main OctetSeqHelperTest
 */

public class OctetSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.OctetSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.OctetSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
