/*
 * @test
 * @summary org.omg.CosNaming.BindingListHelper conformance: helper static id/type/narrow.
 * @run main BindingListHelperTest
 */

public class BindingListHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingListHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.BindingListHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): alias TypeCode + sequence wire channels ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.BindingListHelper.id().equals("IDL:omg.org/CosNaming/BindingList:1.0"), "id() exact");
        org.omg.CORBA.TypeCode blTc = org.omg.CosNaming.BindingListHelper.type();
        check(blTc.kind() == org.omg.CORBA.TCKind.tk_alias, "type().kind() == tk_alias (typedef)");
        check(blTc.name().equals("BindingList"), "alias name BindingList");
        check(blTc.content_type().kind() == org.omg.CORBA.TCKind.tk_sequence, "content type is tk_sequence");
        org.omg.CosNaming.Binding[] two = new org.omg.CosNaming.Binding[]{
            new org.omg.CosNaming.Binding(new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("x", "y")}, org.omg.CosNaming.BindingType.ncontext),
            new org.omg.CosNaming.Binding(new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("p", "q")}, org.omg.CosNaming.BindingType.nobject)};
        org.omg.CORBA.Any anyBl = orb.create_any();
        org.omg.CosNaming.BindingListHelper.insert(anyBl, two);
        check(anyBl.type().kind() == org.omg.CORBA.TCKind.tk_alias, "Any kind tk_alias");
        org.omg.CosNaming.Binding[] rt = org.omg.CosNaming.BindingListHelper.extract(anyBl);
        check(rt.length == 2, "Any round-trip length preserved");
        check(rt[0].binding_name[0].id.equals("x") && rt[0].binding_type == org.omg.CosNaming.BindingType.ncontext
              && rt[1].binding_name[0].id.equals("p") && rt[1].binding_type == org.omg.CosNaming.BindingType.nobject,
              "Any round-trip element-wise");
        org.omg.CORBA.portable.OutputStream osBl = orb.create_output_stream();
        org.omg.CosNaming.BindingListHelper.write(osBl, two);
        org.omg.CosNaming.Binding[] rt2 = org.omg.CosNaming.BindingListHelper.read(osBl.create_input_stream());
        check(rt2.length == 2 && rt2[0].binding_name[0].id.equals("x") && rt2[1].binding_name[0].id.equals("p"),
              "CDR write/read round-trip element-wise");
        // empty sequence survives both channels (length prefix 0)
        org.omg.CORBA.Any anyEmpty = orb.create_any();
        org.omg.CosNaming.BindingListHelper.insert(anyEmpty, new org.omg.CosNaming.Binding[0]);
        check(org.omg.CosNaming.BindingListHelper.extract(anyEmpty).length == 0, "empty array Any round-trip length 0");
        org.omg.CORBA.portable.OutputStream osEmpty = orb.create_output_stream();
        org.omg.CosNaming.BindingListHelper.write(osEmpty, new org.omg.CosNaming.Binding[0]);
        check(org.omg.CosNaming.BindingListHelper.read(osEmpty.create_input_stream()).length == 0, "empty array CDR round-trip length 0");
        // single-element sequence
        org.omg.CORBA.Any anyOne = orb.create_any();
        org.omg.CosNaming.BindingListHelper.insert(anyOne, new org.omg.CosNaming.Binding[]{two[0]});
        org.omg.CosNaming.Binding[] rtOne = org.omg.CosNaming.BindingListHelper.extract(anyOne);
        check(rtOne.length == 1 && rtOne[0].binding_name[0].kind.equals("y"), "single-element Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
