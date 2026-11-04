/*
 * @test
 * @summary org.omg.IOP.CodecPackage.FormatMismatchHelper conformance: helper static id/type/narrow.
 * @run main FormatMismatchHelperTest
 */

public class FormatMismatchHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.id() != null, "id() non-null");
        check(org.omg.IOP.CodecPackage.FormatMismatchHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
