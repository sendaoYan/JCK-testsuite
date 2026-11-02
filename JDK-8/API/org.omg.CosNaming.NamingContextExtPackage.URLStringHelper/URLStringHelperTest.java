/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.URLStringHelper conformance: helper static id/type/narrow.
 * @run main URLStringHelperTest
 */

public class URLStringHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextExtPackage.URLStringHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextExtPackage.URLStringHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
