/*
 * @test
 * @summary org.omg.CORBA.SetOverrideTypeHelper conformance: helper static id/type/narrow.
 * @run main SetOverrideTypeHelperTest
 */

public class SetOverrideTypeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.SetOverrideTypeHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.SetOverrideTypeHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
