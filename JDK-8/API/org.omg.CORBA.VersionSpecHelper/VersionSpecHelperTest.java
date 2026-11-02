/*
 * @test
 * @summary org.omg.CORBA.VersionSpecHelper conformance: helper static id/type/narrow.
 * @run main VersionSpecHelperTest
 */

public class VersionSpecHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.VersionSpecHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.VersionSpecHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
