/*
 * @test
 * @summary org.omg.CosNaming.NameComponentHelper conformance: helper static id/type/narrow.
 * @run main NameComponentHelperTest
 */

public class NameComponentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NameComponentHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NameComponentHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
