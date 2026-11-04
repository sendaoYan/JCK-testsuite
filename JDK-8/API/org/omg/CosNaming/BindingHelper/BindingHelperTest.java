/*
 * @test
 * @summary org.omg.CosNaming.BindingHelper conformance: helper static id/type/narrow.
 * @run main BindingHelperTest
 */

public class BindingHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.BindingHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
