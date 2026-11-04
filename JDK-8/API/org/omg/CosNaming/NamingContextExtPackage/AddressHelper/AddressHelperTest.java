/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.AddressHelper conformance: helper static id/type/narrow.
 * @run main AddressHelperTest
 */

public class AddressHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextExtPackage.AddressHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextExtPackage.AddressHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
