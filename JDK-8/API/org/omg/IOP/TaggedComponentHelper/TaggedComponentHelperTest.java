/*
 * @test
 * @summary org.omg.IOP.TaggedComponentHelper conformance: helper static id/type/narrow.
 * @run main TaggedComponentHelperTest
 */

public class TaggedComponentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.TaggedComponentHelper.id() != null, "id() non-null");
        check(org.omg.IOP.TaggedComponentHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.IOP.TaggedComponentHelper.id().equals("IDL:omg.org/IOP/TaggedComponent:1.0"), "id() exact");
        check(org.omg.IOP.TaggedComponentHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct, "type tk_struct");
        check(org.omg.IOP.TaggedComponentHelper.type().member_count() == 2, "struct 2 members");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.TaggedComponentHelper.write(os, new org.omg.IOP.TaggedComponent(0xABCD, new byte[]{7, 8}));
        org.omg.IOP.TaggedComponent rt = org.omg.IOP.TaggedComponentHelper.read(os.create_input_stream());
        check(rt.tag == 0xABCD && rt.component_data.length == 2 && rt.component_data[1] == 8, "wire round-trip");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.TaggedComponentHelper.insert(a, new org.omg.IOP.TaggedComponent(3, new byte[]{9}));
        org.omg.IOP.TaggedComponent ra = org.omg.IOP.TaggedComponentHelper.extract(a);
        check(ra.tag == 3 && ra.component_data.length == 1, "Any insert/extract round-trip");
        boolean npe = false;
        try { org.omg.IOP.TaggedComponentHelper.insert(a, null); } catch (NullPointerException n) { npe = true; }
        check(npe, "insert(any, null) -> NullPointerException");
        boolean marshal = false;
        try { org.omg.IOP.TaggedComponentHelper.extract(orb.create_any()); }
        catch (org.omg.CORBA.MARSHAL m) { marshal = true; }
        check(marshal, "extract(empty any) -> MARSHAL");
        boolean npeW = false;
        try { org.omg.IOP.TaggedComponentHelper.write(orb.create_output_stream(),
              new org.omg.IOP.TaggedComponent(1, null)); } catch (NullPointerException n) { npeW = true; }
        check(npeW, "write(os, tc-with-null-data) -> NullPointerException");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
