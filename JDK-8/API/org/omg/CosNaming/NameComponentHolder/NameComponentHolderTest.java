/*
 * @test
 * @summary org.omg.CosNaming.NameComponentHolder conformance: holder value field.
 * @run main NameComponentHolderTest
 */

public class NameComponentHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NameComponentHolder h0 = new org.omg.CosNaming.NameComponentHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NameComponent v = null;
        org.omg.CosNaming.NameComponentHolder h1 = new org.omg.CosNaming.NameComponentHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
