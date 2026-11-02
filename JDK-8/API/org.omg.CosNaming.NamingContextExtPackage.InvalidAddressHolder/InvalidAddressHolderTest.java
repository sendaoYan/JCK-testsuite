/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder conformance: holder value field.
 * @run main InvalidAddressHolderTest
 */

public class InvalidAddressHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder h0 = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress v = null;
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder h1 = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
