/*
 * @test
 * @summary org.omg.CosNaming.NameHelper conformance: helper static id/type/narrow.
 * @run main NameHelperTest
 */

public class NameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NameHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NameHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
