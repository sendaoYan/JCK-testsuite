/*
 * @test
 * @summary org.omg.DynamicAny.NameValuePairHelper conformance: helper static id/type/narrow.
 * @run main NameValuePairHelperTest
 */

public class NameValuePairHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.NameValuePairHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.NameValuePairHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
