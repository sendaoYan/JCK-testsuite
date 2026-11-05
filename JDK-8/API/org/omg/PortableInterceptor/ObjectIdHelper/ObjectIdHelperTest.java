/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectIdHelper conformance: helper static id/type/narrow.
 * @run main ObjectIdHelperTest
 */

public class ObjectIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ObjectIdHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ObjectIdHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): alias helper (octet sequence) ----
        check(org.omg.PortableInterceptor.ObjectIdHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ObjectId:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ObjectIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        byte[] oid = {1, 2, 3};
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ObjectIdHelper.write(os, oid);
        byte[] rt = org.omg.PortableInterceptor.ObjectIdHelper.read(os.create_input_stream());
        check(rt.length == 3 && rt[0] == 1 && rt[2] == 3, "wire round-trip octet id");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ObjectIdHelper.insert(a, oid);
        check(org.omg.PortableInterceptor.ObjectIdHelper.extract(a).length == 3, "Any insert/extract round-trip");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.PortableInterceptor.ObjectIdHelper.write(osE, new byte[0]);
        check(org.omg.PortableInterceptor.ObjectIdHelper.read(osE.create_input_stream()).length == 0,
              "empty octet id wire round-trip");
        // NOTE: org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper is a *String* alias instead
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ORBInitInfo/ObjectId:1.0"),
              "ORBInitInfo-package ObjectId is a different (string) alias");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
