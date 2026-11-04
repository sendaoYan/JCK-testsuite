/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotFoundHolder conformance: holder value field.
 * @run main NotFoundHolderTest
 */

public class NotFoundHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextPackage.NotFoundHolder h0 = new org.omg.CosNaming.NamingContextPackage.NotFoundHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextPackage.NotFound v = null;
        org.omg.CosNaming.NamingContextPackage.NotFoundHolder h1 = new org.omg.CosNaming.NamingContextPackage.NotFoundHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
