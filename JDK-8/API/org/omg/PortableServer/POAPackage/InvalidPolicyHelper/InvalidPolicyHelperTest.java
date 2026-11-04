/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.InvalidPolicyHelper conformance: helper static id/type/narrow.
 * @run main InvalidPolicyHelperTest
 */

public class InvalidPolicyHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
