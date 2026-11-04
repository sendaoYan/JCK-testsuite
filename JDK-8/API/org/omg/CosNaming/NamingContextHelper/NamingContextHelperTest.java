/*
 * @test
 * @summary org.omg.CosNaming.NamingContextHelper conformance: helper static id/type/narrow.
 * @run main NamingContextHelperTest
 */

public class NamingContextHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextHelper.type() != null, "type() non-null");
        check(org.omg.CosNaming.NamingContextHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
