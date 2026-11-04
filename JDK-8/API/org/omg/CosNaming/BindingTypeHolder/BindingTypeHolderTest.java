/*
 * @test
 * @summary org.omg.CosNaming.BindingTypeHolder conformance: holder value field.
 * @run main BindingTypeHolderTest
 */

public class BindingTypeHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.BindingTypeHolder h0 = new org.omg.CosNaming.BindingTypeHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.BindingType v = null;
        org.omg.CosNaming.BindingTypeHolder h1 = new org.omg.CosNaming.BindingTypeHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
