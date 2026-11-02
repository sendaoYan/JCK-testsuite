/*
 * @test
 * @summary org.omg.IOP.TaggedProfileHolder conformance: holder value field.
 * @run main TaggedProfileHolderTest
 */

public class TaggedProfileHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.TaggedProfileHolder h0 = new org.omg.IOP.TaggedProfileHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.TaggedProfile v = null;
        org.omg.IOP.TaggedProfileHolder h1 = new org.omg.IOP.TaggedProfileHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
