/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.AdapterNonExistentHelper conformance: helper static id/type/narrow.
 * @run main AdapterNonExistentHelperTest
 */

public class AdapterNonExistentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.AdapterNonExistentHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.AdapterNonExistentHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
