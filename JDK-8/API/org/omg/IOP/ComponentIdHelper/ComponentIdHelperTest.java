/*
 * @test
 * @summary org.omg.IOP.ComponentIdHelper conformance: helper static id/type/narrow.
 * @run main ComponentIdHelperTest
 */

public class ComponentIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ComponentIdHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ComponentIdHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.IOP.ComponentIdHelper.id().equals("IDL:omg.org/IOP/ComponentId:1.0"), "id() exact");
        check(org.omg.IOP.ComponentIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias, "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.ComponentIdHelper.write(os, org.omg.IOP.TAG_ORB_TYPE.value);
        check(org.omg.IOP.ComponentIdHelper.read(os.create_input_stream()) == 0, "wire round-trip TAG_ORB_TYPE 0");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.ComponentIdHelper.insert(a, org.omg.IOP.TAG_JAVA_CODEBASE.value);
        check(org.omg.IOP.ComponentIdHelper.extract(a) == 25, "Any insert/extract round-trip TAG_JAVA_CODEBASE 25");
        org.omg.CORBA.portable.OutputStream os2 = orb.create_output_stream();
        org.omg.IOP.ComponentIdHelper.write(os2, 38);
        check(org.omg.IOP.ComponentIdHelper.read(os2.create_input_stream()) == 38,
              "wire round-trip TAG_RMI_CUSTOM_MAX_STREAM_FORMAT 38");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
