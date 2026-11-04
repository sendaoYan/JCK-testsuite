/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotEmptyHolder conformance: holder value field.
 * @run main NotEmptyHolderTest
 */

public class NotEmptyHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextPackage.NotEmptyHolder h0 = new org.omg.CosNaming.NamingContextPackage.NotEmptyHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextPackage.NotEmpty v = null;
        org.omg.CosNaming.NamingContextPackage.NotEmptyHolder h1 = new org.omg.CosNaming.NamingContextPackage.NotEmptyHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
