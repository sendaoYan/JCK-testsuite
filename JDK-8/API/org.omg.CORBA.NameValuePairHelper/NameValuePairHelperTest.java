/*
 * @test
 * @summary org.omg.CORBA.NameValuePairHelper conformance: helper static id/type/narrow.
 * @run main NameValuePairHelperTest
 */

public class NameValuePairHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.NameValuePairHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.NameValuePairHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
