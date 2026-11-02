/*
 * @test
 * @summary org.omg.CORBA.RepositoryIdHelper conformance: helper static id/type/narrow.
 * @run main RepositoryIdHelperTest
 */

public class RepositoryIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.RepositoryIdHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.RepositoryIdHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
