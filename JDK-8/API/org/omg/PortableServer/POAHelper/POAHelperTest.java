/*
 * @test
 * @summary org.omg.PortableServer.POAHelper conformance: helper static id/type/narrow.
 * @run main POAHelperTest
 */

public class POAHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAHelper.type() != null, "type() non-null");
        check(org.omg.PortableServer.POAHelper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened (jdk8u372-b07 probed): repo id 版本 / tk_objref / narrow null / MARSHAL 负例矩阵 ----
        check(org.omg.PortableServer.POAHelper.id().equals("IDL:omg.org/PortableServer/POA:2.3"), "id exact (repo id version 2.3)");
        check(org.omg.PortableServer.POAHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref, "type().kind() == tk_objref");
        check(org.omg.PortableServer.POAHelper.type().id().equals(org.omg.PortableServer.POAHelper.id()), "type().id() == id()");
        check(org.omg.PortableServer.POAHelper.type().name().equals("POA"), "type().name() == POA");
        try {
            org.omg.PortableServer.POAHelper.class.getMethod("unchecked_narrow", org.omg.CORBA.Object.class);
            check(false, "unexpected: POAHelper declares unchecked_narrow");
        } catch (NoSuchMethodException expected) {
            check(true, "old-mapping trap: POAHelper has narrow() but NO unchecked_narrow");
        }
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        try {
            org.omg.PortableServer.POAHelper.read(orb.create_output_stream().create_input_stream());
            check(false, "read of empty stream must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "read(empty stream) -> MARSHAL minor==0");
        }
        try {
            org.omg.PortableServer.POAHelper.extract(orb.create_any());
            check(false, "extract of empty Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "extract(empty Any) -> MARSHAL minor==0");
        }
        try {
            org.omg.PortableServer.POAHelper.write(orb.create_output_stream(), null);
            check(false, "write of null objref must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0,
                  "write(null) -> MARSHAL minor==0 (helper rejects null; raw write_Object(null) is legal)");
        }
        try {
            org.omg.PortableServer.POAHelper.insert(orb.create_any(), null);
            check(false, "insert of null objref must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "insert(any, null) -> MARSHAL minor==0");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
