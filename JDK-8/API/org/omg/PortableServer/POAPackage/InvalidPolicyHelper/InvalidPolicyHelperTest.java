/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.InvalidPolicyHelper conformance: helper static id/type/narrow.
 * @run main InvalidPolicyHelperTest
 */

public class InvalidPolicyHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): id exact / tk_except / index 字段过 Any 与 CDR 保真 ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(java.lang.reflect.Modifier.isAbstract(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.class.getModifiers()),
              "helper is an abstract class (instantiable subclass face)");
        check(!org.omg.PortableServer.POAPackage.InvalidPolicyHelper.class.isInterface(), "helper is not an interface");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.id().equals(
                  "IDL:omg.org/PortableServer/POA/InvalidPolicy:1.0"), "id exact");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "type().kind() == tk_except");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.type().id().equals(
                  "IDL:omg.org/PortableServer/POA/InvalidPolicy:1.0"), "type().id() == id()");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.type().member_count() == 1,
              "exception TypeCode member_count == 1");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.type().member_name(0).equals("index"),
              "member_name(0) == index");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.type().member_type(0).kind() == org.omg.CORBA.TCKind.tk_ushort,
              "member index maps to tk_ushort (IDL unsigned short; Java field is signed short)");
        org.omg.CORBA.Any hAny = orb.create_any();
        org.omg.PortableServer.POAPackage.InvalidPolicyHelper.insert(
            hAny, new org.omg.PortableServer.POAPackage.InvalidPolicy("boom", (short) 9));
        check(hAny.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any kind tk_except after insert");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.extract(hAny).getClass()
                  == org.omg.PortableServer.POAPackage.InvalidPolicy.class, "Any round-trip preserves class");
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.extract(hAny).index == 9,
              "Any round-trip preserves index field");
        org.omg.CORBA.portable.OutputStream hos = orb.create_output_stream();
        org.omg.PortableServer.POAPackage.InvalidPolicyHelper.write(
            hos, new org.omg.PortableServer.POAPackage.InvalidPolicy((short) -3));
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.read(hos.create_input_stream()).index == -3,
              "CDR round-trip preserves negative index");
        hos = orb.create_output_stream();
        org.omg.PortableServer.POAPackage.InvalidPolicyHelper.write(hos, new org.omg.PortableServer.POAPackage.InvalidPolicy((short) 1));
        org.omg.PortableServer.POAPackage.InvalidPolicyHelper.write(hos, new org.omg.PortableServer.POAPackage.InvalidPolicy((short) 2));
        org.omg.CORBA.portable.InputStream his = hos.create_input_stream();
        check(org.omg.PortableServer.POAPackage.InvalidPolicyHelper.read(his).index == 1
           && org.omg.PortableServer.POAPackage.InvalidPolicyHelper.read(his).index == 2,
              "two back-to-back exceptions in one CDR stream keep order");
        try {
            org.omg.PortableServer.POAPackage.InvalidPolicyHelper.extract(orb.create_any());
            check(false, "extract of empty Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "extract(empty Any) -> org.omg.CORBA.MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
