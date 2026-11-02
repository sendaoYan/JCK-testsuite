/*
 * @test
 * @summary org.omg.IOP.TaggedComponentHolder conformance: holder value field.
 * @run main TaggedComponentHolderTest
 */

public class TaggedComponentHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.TaggedComponentHolder h0 = new org.omg.IOP.TaggedComponentHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.TaggedComponent v = null;
        org.omg.IOP.TaggedComponentHolder h1 = new org.omg.IOP.TaggedComponentHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
