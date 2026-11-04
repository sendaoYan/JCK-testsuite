/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHelper conformance: helper static id/type/narrow.
 * @run main InvalidAddressHelperTest
 */

public class InvalidAddressHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
