/*
 * @test
 * @summary org.omg.CosNaming.NameHelper conformance: helper static id/type/narrow.
 * @run main NameHelperTest
 */

public class NameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NameHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NameHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): alias of sequence<NameComponent> ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.NameHelper.id().equals("IDL:omg.org/CosNaming/Name:1.0"), "id() exact");
        org.omg.CORBA.TypeCode nTc = org.omg.CosNaming.NameHelper.type();
        check(nTc.kind() == org.omg.CORBA.TCKind.tk_alias, "type().kind() == tk_alias (typedef)");
        check(nTc.name().equals("Name"), "alias name Name");
        check(nTc.content_type().kind() == org.omg.CORBA.TCKind.tk_sequence, "content tk_sequence");
        org.omg.CosNaming.NameComponent[] nm = new org.omg.CosNaming.NameComponent[]{
            new org.omg.CosNaming.NameComponent("a", "b"), new org.omg.CosNaming.NameComponent("c", "d")};
        org.omg.CORBA.Any anyN = orb.create_any();
        org.omg.CosNaming.NameHelper.insert(anyN, nm);
        check(anyN.type().kind() == org.omg.CORBA.TCKind.tk_alias, "Any kind tk_alias");
        org.omg.CosNaming.NameComponent[] rt = org.omg.CosNaming.NameHelper.extract(anyN);
        check(rt.length == 2 && rt[0].id.equals("a") && rt[0].kind.equals("b")
              && rt[1].id.equals("c") && rt[1].kind.equals("d"), "Any round-trip element-wise");
        org.omg.CORBA.portable.OutputStream osN = orb.create_output_stream();
        org.omg.CosNaming.NameHelper.write(osN, nm);
        org.omg.CosNaming.NameComponent[] rt2 = org.omg.CosNaming.NameHelper.read(osN.create_input_stream());
        check(rt2.length == 2 && rt2[1].id.equals("c") && rt2[1].kind.equals("d"), "CDR round-trip element-wise");
        org.omg.CORBA.portable.OutputStream osEmpty = orb.create_output_stream();
        org.omg.CosNaming.NameHelper.write(osEmpty, new org.omg.CosNaming.NameComponent[0]);
        check(org.omg.CosNaming.NameHelper.read(osEmpty.create_input_stream()).length == 0, "empty name CDR round-trip");
        org.omg.CORBA.Any anyOne = orb.create_any();
        org.omg.CosNaming.NameHelper.insert(anyOne, new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("solo", "k")});
        check(org.omg.CosNaming.NameHelper.extract(anyOne).length == 1
              && org.omg.CosNaming.NameHelper.extract(anyOne)[0].id.equals("solo"), "single-component Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
