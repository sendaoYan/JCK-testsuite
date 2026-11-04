/*
 * @test
 * @summary org.omg.IOP.TaggedProfile conformance: value type fields and constructors.
 * @run main TaggedProfileTest
 */

public class TaggedProfileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.TaggedProfile s0 = new org.omg.IOP.TaggedProfile();
        check(s0.tag == 0, "no-arg tag default");
        check(s0.profile_data == null, "no-arg profile_data default");
        org.omg.IOP.TaggedProfile s1 = new org.omg.IOP.TaggedProfile(7, null);
        check(s1.tag == 7, "ctor tag stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
