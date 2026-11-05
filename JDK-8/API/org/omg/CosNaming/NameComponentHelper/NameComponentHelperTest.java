/*
 * @test
 * @summary org.omg.CosNaming.NameComponentHelper conformance: helper static id/type/narrow.
 * @run main NameComponentHelperTest
 */

public class NameComponentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NameComponentHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NameComponentHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): repository id, TypeCode shape, Any/CDR channels ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.NameComponentHelper.id().equals("IDL:omg.org/CosNaming/NameComponent:1.0"),
              "id() exact repository id string");
        org.omg.CORBA.TypeCode ncTc = org.omg.CosNaming.NameComponentHelper.type();
        check(ncTc.kind() == org.omg.CORBA.TCKind.tk_struct, "type().kind() == tk_struct");
        check(ncTc.id().equals(org.omg.CosNaming.NameComponentHelper.id()), "type().id() equals Helper.id()");
        check(ncTc.name().equals("NameComponent"), "type().name() == NameComponent");
        check(ncTc.member_count() == 2, "struct member_count == 2");
        check(ncTc.member_name(0).equals("id") && ncTc.member_name(1).equals("kind"), "member names id,kind");
        check(ncTc.member_type(0).kind() == org.omg.CORBA.TCKind.tk_alias, "member id is alias Istring (tk_alias)");
        // Any channel: insert/extract returns a NEW instance (identity equals is false) -> field-wise compare
        org.omg.CORBA.Any anyNc = orb.create_any();
        org.omg.CosNaming.NameComponent inNc = new org.omg.CosNaming.NameComponent("aid", "akind");
        org.omg.CosNaming.NameComponentHelper.insert(anyNc, inNc);
        check(anyNc.type().kind() == org.omg.CORBA.TCKind.tk_struct, "Any carrying NameComponent has kind tk_struct");
        org.omg.CosNaming.NameComponent outNc = org.omg.CosNaming.NameComponentHelper.extract(anyNc);
        check(outNc.id.equals("aid") && outNc.kind.equals("akind"), "Any insert/extract round-trip field-wise");
        check(!outNc.equals(inNc), "extracted instance is a fresh object (identity equals, not value equals)");
        // CDR stream channel
        org.omg.CORBA.portable.OutputStream osNc = orb.create_output_stream();
        org.omg.CosNaming.NameComponentHelper.write(osNc, inNc);
        org.omg.CosNaming.NameComponent fromStream = org.omg.CosNaming.NameComponentHelper.read(osNc.create_input_stream());
        check(fromStream.id.equals("aid") && fromStream.kind.equals("akind"), "CDR write/read round-trip field-wise");
        // negative: extract from a tk_long Any -> MARSHAL
        org.omg.CORBA.Any wrongAny = orb.create_any();
        wrongAny.insert_long(3);
        try {
            org.omg.CosNaming.NameComponentHelper.extract(wrongAny);
            throw new AssertionError("extract over tk_long Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "extract over tk_long Any -> org.omg.CORBA.MARSHAL");
        }
        // empty-string fields survive the Any channel
        org.omg.CORBA.Any anyEmpty = orb.create_any();
        org.omg.CosNaming.NameComponentHelper.insert(anyEmpty, new org.omg.CosNaming.NameComponent("", ""));
        org.omg.CosNaming.NameComponent outEmpty = org.omg.CosNaming.NameComponentHelper.extract(anyEmpty);
        check(outEmpty.id.isEmpty() && outEmpty.kind.isEmpty(), "empty id/kind strings survive Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
