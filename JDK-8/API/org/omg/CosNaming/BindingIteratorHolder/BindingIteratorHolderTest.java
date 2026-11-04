/*
 * @test
 * @summary org.omg.CosNaming.BindingIteratorHolder conformance: holder value field.
 * @run main BindingIteratorHolderTest
 */

public class BindingIteratorHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.BindingIteratorHolder h0 = new org.omg.CosNaming.BindingIteratorHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.BindingIterator v = null;
        org.omg.CosNaming.BindingIteratorHolder h1 = new org.omg.CosNaming.BindingIteratorHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
