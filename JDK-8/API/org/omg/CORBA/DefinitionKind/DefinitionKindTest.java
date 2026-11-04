/*
 * @test
 * @summary org.omg.CORBA.DefinitionKind conformance: enumeration values.
 * @run main DefinitionKindTest
 */

public class DefinitionKindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.DefinitionKind.dk_none != null, "dk_none constant non-null");
        check(org.omg.CORBA.DefinitionKind.dk_none.value() == org.omg.CORBA.DefinitionKind._dk_none, "value() matches int constant");
        check(org.omg.CORBA.DefinitionKind.from_int(org.omg.CORBA.DefinitionKind._dk_none) == org.omg.CORBA.DefinitionKind.dk_none, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
