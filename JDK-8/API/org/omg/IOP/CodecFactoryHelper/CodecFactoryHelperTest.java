/*
 * @test
 * @summary org.omg.IOP.CodecFactoryHelper conformance: helper static id/type/narrow.
 * @run main CodecFactoryHelperTest
 */

public class CodecFactoryHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.CodecFactoryHelper.id() != null, "id() non-null");
        check(org.omg.IOP.CodecFactoryHelper.type() != null, "type() non-null");
        check(org.omg.IOP.CodecFactoryHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
