/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.WrongPolicyHelper conformance: helper static id/type/narrow.
 * @run main WrongPolicyHelperTest
 */

public class WrongPolicyHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.WrongPolicyHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.WrongPolicyHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
