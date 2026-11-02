/*
 * @test
 * @summary org.omg.CosNaming.BindingListHolder conformance: holder value field.
 * @run main BindingListHolderTest
 */

public class BindingListHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.BindingListHolder h0 = new org.omg.CosNaming.BindingListHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.Binding[] v = null;
        org.omg.CosNaming.BindingListHolder h1 = new org.omg.CosNaming.BindingListHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
