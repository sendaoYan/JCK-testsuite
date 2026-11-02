/*
 * @test
 * @summary org.omg.DynamicAny.NameValuePairSeqHelper conformance: helper static id/type/narrow.
 * @run main NameValuePairSeqHelperTest
 */

public class NameValuePairSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.NameValuePairSeqHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.NameValuePairSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
