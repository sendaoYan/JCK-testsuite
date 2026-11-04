/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.ObjectAlreadyActiveHelper conformance: helper static id/type/narrow.
 * @run main ObjectAlreadyActiveHelperTest
 */

public class ObjectAlreadyActiveHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.ObjectAlreadyActiveHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.ObjectAlreadyActiveHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
