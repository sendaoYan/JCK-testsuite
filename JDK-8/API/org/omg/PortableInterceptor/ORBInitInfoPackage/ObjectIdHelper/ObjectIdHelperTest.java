/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper conformance: helper static id/type/narrow.
 * @run main ObjectIdHelperTest
 */

public class ObjectIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): this ObjectId is a STRING alias ----
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ORBInitInfo/ObjectId:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.write(os, "str-oid");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.read(os.create_input_stream()).equals("str-oid"),
              "wire round-trip String object id");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.insert(a, "s2");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper.extract(a).equals("s2"),
              "Any insert/extract round-trip");
        // contrast: the PortableInterceptor-package ObjectId is an octet-sequence alias
        java.lang.reflect.Method w = org.omg.PortableInterceptor.ObjectIdHelper.class.getMethod(
            "write", org.omg.CORBA.portable.OutputStream.class, byte[].class);
        check(w != null, "PI.ObjectIdHelper.write takes byte[] while ORBInitInfoPackage.ObjectIdHelper takes String");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
