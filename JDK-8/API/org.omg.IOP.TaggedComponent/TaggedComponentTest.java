/*
 * @test
 * @summary org.omg.IOP.TaggedComponent conformance: value type fields and constructors.
 * @run main TaggedComponentTest
 */

public class TaggedComponentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.TaggedComponent s0 = new org.omg.IOP.TaggedComponent();
        check(s0.tag == 0, "no-arg tag default");
        check(s0.component_data == null, "no-arg component_data default");
        org.omg.IOP.TaggedComponent s1 = new org.omg.IOP.TaggedComponent(7, null);
        check(s1.tag == 7, "ctor tag stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
