/*
 * @test
 * @summary org.omg.CORBA.IdentifierHelper conformance: helper static id/type/narrow.
 * @run main IdentifierHelperTest
 */

public class IdentifierHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.IdentifierHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.IdentifierHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
