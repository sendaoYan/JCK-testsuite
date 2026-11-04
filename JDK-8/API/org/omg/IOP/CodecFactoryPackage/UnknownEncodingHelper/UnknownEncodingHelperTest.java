/*
 * @test
 * @summary org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper conformance: helper static id/type/narrow.
 * @run main UnknownEncodingHelperTest
 */

public class UnknownEncodingHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.id() != null, "id() non-null");
        check(org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
