/*
 * @test
 * @summary org.omg.IOP.IORHelper conformance: helper static id/type/narrow.
 * @run main IORHelperTest
 */

public class IORHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.IORHelper.id() != null, "id() non-null");
        check(org.omg.IOP.IORHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): repository id / type / CDR / Any contract ----
        check(org.omg.IOP.IORHelper.id().equals("IDL:omg.org/IOP/IOR:1.0"), "id() exact repository id");
        check(org.omg.IOP.IORHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct, "type().kind() == tk_struct");
        check(org.omg.IOP.IORHelper.type().member_count() == 2, "type(): struct with 2 members");
        check(org.omg.IOP.IORHelper.type().member_name(0).equals("type_id")
              && org.omg.IOP.IORHelper.type().member_name(1).equals("profiles"), "type(): member names exact");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.IOP.TaggedProfile[] ps = {
            new org.omg.IOP.TaggedProfile(0, new byte[]{1, 2}),
            new org.omg.IOP.TaggedProfile(1, new byte[]{3})};
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.IORHelper.write(os, new org.omg.IOP.IOR("IDL:W:1.0", ps));
        org.omg.IOP.IOR back = org.omg.IOP.IORHelper.read(os.create_input_stream());
        check(back.type_id.equals("IDL:W:1.0") && back.profiles.length == 2
              && back.profiles[0].tag == 0 && back.profiles[1].profile_data[0] == 3,
              "CDR write/read round-trip preserves all fields");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.IORHelper.insert(a, new org.omg.IOP.IOR("IDL:A:1.0", ps));
        check(org.omg.IOP.IORHelper.extract(a).type_id.equals("IDL:A:1.0"), "Any insert/extract round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_struct, "Any type after insert is tk_struct");
        boolean npe = false;
        try { org.omg.IOP.IORHelper.insert(orb.create_any(), null); } catch (NullPointerException n) { npe = true; }
        check(npe, "insert(any, null) -> NullPointerException");
        boolean marshal = false;
        try { org.omg.IOP.IORHelper.extract(orb.create_any()); }
        catch (org.omg.CORBA.MARSHAL m) { marshal = true; }
        check(marshal, "extract(empty any) -> org.omg.CORBA.MARSHAL");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
