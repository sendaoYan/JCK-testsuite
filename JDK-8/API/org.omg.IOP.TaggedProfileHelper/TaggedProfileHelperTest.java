/*
 * @test
 * @summary org.omg.IOP.TaggedProfileHelper conformance: helper static id/type/narrow.
 * @run main TaggedProfileHelperTest
 */

public class TaggedProfileHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.TaggedProfileHelper.id() != null, "id() non-null");
        check(org.omg.IOP.TaggedProfileHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
