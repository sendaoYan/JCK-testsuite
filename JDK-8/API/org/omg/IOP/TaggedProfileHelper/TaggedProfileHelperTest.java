/*
 * @test
 * @summary org.omg.IOP.TaggedProfileHelper conformance: helper static id/type/narrow.
 * @run main TaggedProfileHelperTest
 */

public class TaggedProfileHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.TaggedProfileHelper.id() != null, "id() non-null");
        check(org.omg.IOP.TaggedProfileHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.IOP.TaggedProfileHelper.id().equals("IDL:omg.org/IOP/TaggedProfile:1.0"), "id() exact");
        check(org.omg.IOP.TaggedProfileHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct, "type tk_struct");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.TaggedProfileHelper.write(os, new org.omg.IOP.TaggedProfile(1, new byte[]{3, 4, 5}));
        org.omg.IOP.TaggedProfile rt = org.omg.IOP.TaggedProfileHelper.read(os.create_input_stream());
        check(rt.tag == 1 && rt.profile_data.length == 3 && rt.profile_data[2] == 5, "wire round-trip");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.TaggedProfileHelper.insert(a, new org.omg.IOP.TaggedProfile(2, new byte[]{6}));
        check(org.omg.IOP.TaggedProfileHelper.extract(a).tag == 2, "Any insert/extract round-trip");
        boolean npe = false;
        try { org.omg.IOP.TaggedProfileHelper.write(orb.create_output_stream(),
              new org.omg.IOP.TaggedProfile(1, null)); } catch (NullPointerException n) { npe = true; }
        check(npe, "write with null profile_data -> NullPointerException");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
