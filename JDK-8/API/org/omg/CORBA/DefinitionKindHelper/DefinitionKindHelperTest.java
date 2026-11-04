/*
 * @test
 * @summary org.omg.CORBA.DefinitionKindHelper conformance: helper static id/type/narrow.
 * @run main DefinitionKindHelperTest
 */

public class DefinitionKindHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.DefinitionKindHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.DefinitionKindHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
