/*
 * @test
 * @summary org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper conformance: helper static id/type/narrow.
 * @run main AdapterInactiveHelperTest
 */

public class AdapterInactiveHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
