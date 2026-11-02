/*
 * @test
 * @summary org.omg.CosNaming.BindingHolder conformance: holder value field.
 * @run main BindingHolderTest
 */

public class BindingHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.BindingHolder h0 = new org.omg.CosNaming.BindingHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.Binding v = null;
        org.omg.CosNaming.BindingHolder h1 = new org.omg.CosNaming.BindingHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
