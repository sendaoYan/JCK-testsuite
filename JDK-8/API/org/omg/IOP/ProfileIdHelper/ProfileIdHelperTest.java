/*
 * @test
 * @summary org.omg.IOP.ProfileIdHelper conformance: helper static id/type/narrow.
 * @run main ProfileIdHelperTest
 */

public class ProfileIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ProfileIdHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ProfileIdHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.IOP.ProfileIdHelper.id().equals("IDL:omg.org/IOP/ProfileId:1.0"), "id() exact");
        check(org.omg.IOP.ProfileIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias, "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.ProfileIdHelper.write(os, org.omg.IOP.TAG_INTERNET_IOP.value);
        check(org.omg.IOP.ProfileIdHelper.read(os.create_input_stream()) == 0, "wire round-trip value 0");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.ProfileIdHelper.insert(a, org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value);
        check(org.omg.IOP.ProfileIdHelper.extract(a) == 1, "Any insert/extract round-trip value 1");
        org.omg.CORBA.portable.OutputStream os2 = orb.create_output_stream();
        org.omg.IOP.ProfileIdHelper.write(os2, 0x7FFFFFFF);
        check(org.omg.IOP.ProfileIdHelper.read(os2.create_input_stream()) == 0x7FFFFFFF, "wire round-trip Integer.MAX_VALUE");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
