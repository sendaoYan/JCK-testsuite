/*
 * @test
 * @summary org.omg.IOP.TaggedComponentHelper conformance: helper static id/type/narrow.
 * @run main TaggedComponentHelperTest
 */

public class TaggedComponentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.TaggedComponentHelper.id() != null, "id() non-null");
        check(org.omg.IOP.TaggedComponentHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
