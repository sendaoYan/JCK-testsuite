/*
 * @test
 * @summary org.omg.IOP.MultipleComponentProfileHelper conformance: helper static id/type/narrow.
 * @run main MultipleComponentProfileHelperTest
 */

public class MultipleComponentProfileHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.MultipleComponentProfileHelper.id() != null, "id() non-null");
        check(org.omg.IOP.MultipleComponentProfileHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.IOP.MultipleComponentProfileHelper.id().equals("IDL:omg.org/IOP/MultipleComponentProfile:1.0"),
              "id() exact");
        check(org.omg.IOP.MultipleComponentProfileHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.IOP.TaggedComponent[] mcp = {
            new org.omg.IOP.TaggedComponent(20, new byte[]{1}),
            new org.omg.IOP.TaggedComponent(25, new byte[]{2, 3})};
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.MultipleComponentProfileHelper.write(os, mcp);
        org.omg.IOP.TaggedComponent[] rt = org.omg.IOP.MultipleComponentProfileHelper.read(os.create_input_stream());
        check(rt.length == 2 && rt[0].tag == 20 && rt[1].tag == 25 && rt[1].component_data[1] == 3,
              "wire round-trip sequence of TaggedComponent");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.MultipleComponentProfileHelper.insert(a, mcp);
        check(org.omg.IOP.MultipleComponentProfileHelper.extract(a).length == 2, "Any insert/extract round-trip");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.IOP.MultipleComponentProfileHelper.write(osE, new org.omg.IOP.TaggedComponent[0]);
        check(org.omg.IOP.MultipleComponentProfileHelper.read(osE.create_input_stream()).length == 0,
              "empty profile wire round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
