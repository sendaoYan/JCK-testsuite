/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.AlreadyBoundHolder conformance: holder value field.
 * @run main AlreadyBoundHolderTest
 */

public class AlreadyBoundHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextPackage.AlreadyBoundHolder h0 = new org.omg.CosNaming.NamingContextPackage.AlreadyBoundHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextPackage.AlreadyBound v = null;
        org.omg.CosNaming.NamingContextPackage.AlreadyBoundHolder h1 = new org.omg.CosNaming.NamingContextPackage.AlreadyBoundHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
