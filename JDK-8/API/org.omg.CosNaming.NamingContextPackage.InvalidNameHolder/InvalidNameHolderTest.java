/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.InvalidNameHolder conformance: holder value field.
 * @run main InvalidNameHolderTest
 */

public class InvalidNameHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextPackage.InvalidNameHolder h0 = new org.omg.CosNaming.NamingContextPackage.InvalidNameHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextPackage.InvalidName v = null;
        org.omg.CosNaming.NamingContextPackage.InvalidNameHolder h1 = new org.omg.CosNaming.NamingContextPackage.InvalidNameHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
