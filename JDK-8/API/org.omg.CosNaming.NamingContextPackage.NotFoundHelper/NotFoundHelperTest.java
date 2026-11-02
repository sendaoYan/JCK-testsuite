/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotFoundHelper conformance: helper static id/type/narrow.
 * @run main NotFoundHelperTest
 */

public class NotFoundHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextPackage.NotFoundHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
