/*
 * @test
 * @summary org.omg.CosNaming.BindingTypeHelper conformance: helper static id/type/narrow.
 * @run main BindingTypeHelperTest
 */

public class BindingTypeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingTypeHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.BindingTypeHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): enum helper over Any/CDR + negative ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.BindingTypeHelper.id().equals("IDL:omg.org/CosNaming/BindingType:1.0"), "id() exact");
        org.omg.CORBA.TypeCode btTc = org.omg.CosNaming.BindingTypeHelper.type();
        check(btTc.kind() == org.omg.CORBA.TCKind.tk_enum, "type().kind() == tk_enum");
        check(btTc.member_count() == 2, "enum member_count == 2");
        check(btTc.member_name(0).equals("nobject") && btTc.member_name(1).equals("ncontext"), "enum member names");
        org.omg.CORBA.Any anyBt = orb.create_any();
        org.omg.CosNaming.BindingTypeHelper.insert(anyBt, org.omg.CosNaming.BindingType.ncontext);
        check(anyBt.type().kind() == org.omg.CORBA.TCKind.tk_enum, "Any kind tk_enum");
        check(org.omg.CosNaming.BindingTypeHelper.extract(anyBt) == org.omg.CosNaming.BindingType.ncontext,
              "Any round-trip returns the SAME enum constant (normalization)");
        org.omg.CORBA.portable.OutputStream osBt = orb.create_output_stream();
        org.omg.CosNaming.BindingTypeHelper.write(osBt, org.omg.CosNaming.BindingType.nobject);
        check(org.omg.CosNaming.BindingTypeHelper.read(osBt.create_input_stream()) == org.omg.CosNaming.BindingType.nobject,
              "CDR write/read round-trip returns the SAME constant");
        org.omg.CORBA.Any wrongAny = orb.create_any();
        wrongAny.insert_long(3);
        try {
            org.omg.CosNaming.BindingTypeHelper.extract(wrongAny);
            throw new AssertionError("extract over tk_long Any must throw");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "extract over tk_long Any -> org.omg.CORBA.BAD_PARAM");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
