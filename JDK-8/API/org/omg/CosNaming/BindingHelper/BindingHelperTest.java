/*
 * @test
 * @summary org.omg.CosNaming.BindingHelper conformance: helper static id/type/narrow.
 * @run main BindingHelperTest
 */

public class BindingHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.BindingHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): TypeCode shape + dual wire channels ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.BindingHelper.id().equals("IDL:omg.org/CosNaming/Binding:1.0"), "id() exact repository id");
        org.omg.CORBA.TypeCode bTc = org.omg.CosNaming.BindingHelper.type();
        check(bTc.kind() == org.omg.CORBA.TCKind.tk_struct, "type().kind() == tk_struct");
        check(bTc.name().equals("Binding") && bTc.id().equals(org.omg.CosNaming.BindingHelper.id()), "type name/id match");
        check(bTc.member_count() == 2, "member_count == 2");
        check(bTc.member_name(0).equals("binding_name") && bTc.member_name(1).equals("binding_type"), "member names binding_name,binding_type");
        org.omg.CosNaming.Binding inB = new org.omg.CosNaming.Binding(
            new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("x", "y")},
            org.omg.CosNaming.BindingType.ncontext);
        org.omg.CORBA.Any anyB = orb.create_any();
        org.omg.CosNaming.BindingHelper.insert(anyB, inB);
        check(anyB.type().kind() == org.omg.CORBA.TCKind.tk_struct, "Any kind tk_struct");
        org.omg.CosNaming.Binding outB = org.omg.CosNaming.BindingHelper.extract(anyB);
        check(outB.binding_name.length == 1 && outB.binding_name[0].id.equals("x")
              && outB.binding_name[0].kind.equals("y") && outB.binding_type == org.omg.CosNaming.BindingType.ncontext,
              "Any insert/extract round-trip field-wise");
        check(!outB.equals(inB), "extract yields fresh instance (identity equals false)");
        org.omg.CORBA.portable.OutputStream osB = orb.create_output_stream();
        org.omg.CosNaming.BindingHelper.write(osB, inB);
        org.omg.CosNaming.Binding fromStream = org.omg.CosNaming.BindingHelper.read(osB.create_input_stream());
        check(fromStream.binding_name.length == 1 && fromStream.binding_name[0].id.equals("x")
              && fromStream.binding_type == org.omg.CosNaming.BindingType.ncontext, "CDR write/read round-trip field-wise");
        // two bindings back-to-back in one stream
        org.omg.CORBA.portable.OutputStream osB2 = orb.create_output_stream();
        org.omg.CosNaming.BindingHelper.write(osB2, inB);
        org.omg.CosNaming.BindingHelper.write(osB2, new org.omg.CosNaming.Binding(
            new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("p", "q")},
            org.omg.CosNaming.BindingType.nobject));
        org.omg.CORBA.portable.InputStream isB2 = osB2.create_input_stream();
        org.omg.CosNaming.Binding first = org.omg.CosNaming.BindingHelper.read(isB2);
        org.omg.CosNaming.Binding second = org.omg.CosNaming.BindingHelper.read(isB2);
        check(first.binding_name[0].id.equals("x") && second.binding_name[0].id.equals("p")
              && first.binding_type == org.omg.CosNaming.BindingType.ncontext
              && second.binding_type == org.omg.CosNaming.BindingType.nobject,
              "two Bindings back-to-back in one CDR stream");
        org.omg.CORBA.Any wrongAny = orb.create_any();
        wrongAny.insert_long(3);
        try {
            org.omg.CosNaming.BindingHelper.extract(wrongAny);
            throw new AssertionError("extract over tk_long Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "extract over tk_long Any -> org.omg.CORBA.MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
