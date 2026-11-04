/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.CannotProceedHolder conformance: holder value field.
 * @run main CannotProceedHolderTest
 */

public class CannotProceedHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextPackage.CannotProceedHolder h0 = new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextPackage.CannotProceed v = null;
        org.omg.CosNaming.NamingContextPackage.CannotProceedHolder h1 = new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
