/*
 * @test
 * @summary org.omg.CosNaming.NamingContextHolder conformance: holder value field.
 * @run main NamingContextHolderTest
 */

public class NamingContextHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextHolder h0 = new org.omg.CosNaming.NamingContextHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContext v = null;
        org.omg.CosNaming.NamingContextHolder h1 = new org.omg.CosNaming.NamingContextHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
