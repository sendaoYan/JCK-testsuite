/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder conformance: holder value field.
 * @run main NotFoundReasonHolderTest
 */

public class NotFoundReasonHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder h0 = new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextPackage.NotFoundReason v = null;
        org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder h1 = new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
