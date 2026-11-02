/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper conformance: helper static id/type/narrow.
 * @run main AlreadyBoundHelperTest
 */

public class AlreadyBoundHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
