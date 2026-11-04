/*
 * @test
 * @summary org.omg.CORBA.CurrentHelper conformance: helper static id/type/narrow.
 * @run main CurrentHelperTest
 */

public class CurrentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.CurrentHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.CurrentHelper.type() != null, "type() non-null");
        check(org.omg.CORBA.CurrentHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
