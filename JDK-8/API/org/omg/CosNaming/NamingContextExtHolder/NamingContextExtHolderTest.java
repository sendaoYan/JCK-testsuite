/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtHolder conformance: holder value field.
 * @run main NamingContextExtHolderTest
 */

public class NamingContextExtHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextExtHolder h0 = new org.omg.CosNaming.NamingContextExtHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextExt v = null;
        org.omg.CosNaming.NamingContextExtHolder h1 = new org.omg.CosNaming.NamingContextExtHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
