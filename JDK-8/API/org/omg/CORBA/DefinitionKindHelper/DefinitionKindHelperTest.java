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
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.DefinitionKindHelper.id().equals("IDL:omg.org/CORBA/DefinitionKind:1.0"),
              "DefinitionKindHelper.id() exact repository id");
        check(org.omg.CORBA.DefinitionKindHelper.type().kind() == org.omg.CORBA.TCKind.tk_enum,
              "DefinitionKindHelper.type().kind() == tk_enum");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.CORBA.DefinitionKindHelper.write(osE, org.omg.CORBA.DefinitionKind.dk_Interface);
        check(org.omg.CORBA.DefinitionKindHelper.read(osE.create_input_stream()) == org.omg.CORBA.DefinitionKind.dk_Interface,
              "DefinitionKindHelper.write/read round-trips to the canonical constant (identity)");
        org.omg.CORBA.Any anyE = orb.create_any();
        org.omg.CORBA.DefinitionKindHelper.insert(anyE, org.omg.CORBA.DefinitionKind.dk_Attribute);
        check(org.omg.CORBA.DefinitionKindHelper.extract(anyE) == org.omg.CORBA.DefinitionKind.dk_Attribute,
              "DefinitionKindHelper.insert/extract round-trips to the canonical constant (identity)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
