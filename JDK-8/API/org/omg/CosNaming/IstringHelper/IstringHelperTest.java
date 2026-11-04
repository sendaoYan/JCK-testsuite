/*
 * @test
 * @summary org.omg.CosNaming.IstringHelper conformance: helper static id/type/narrow.
 * @run main IstringHelperTest
 */

public class IstringHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.IstringHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.IstringHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
