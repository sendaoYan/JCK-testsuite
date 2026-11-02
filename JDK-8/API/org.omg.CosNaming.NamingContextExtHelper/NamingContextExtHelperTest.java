/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtHelper conformance: helper static id/type/narrow.
 * @run main NamingContextExtHelperTest
 */

public class NamingContextExtHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextExtHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextExtHelper.type() != null, "type() non-null");
        check(org.omg.CosNaming.NamingContextExtHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
