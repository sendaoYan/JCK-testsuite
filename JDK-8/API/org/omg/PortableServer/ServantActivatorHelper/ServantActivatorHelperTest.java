/*
 * @test
 * @summary org.omg.PortableServer.ServantActivatorHelper conformance: helper static id/type/narrow.
 * @run main ServantActivatorHelperTest
 */

public class ServantActivatorHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.ServantActivatorHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.ServantActivatorHelper.type() != null, "type() non-null");
        check(org.omg.PortableServer.ServantActivatorHelper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened (jdk8u372-b07 probed): repo id 版本 / tk_objref / narrow null / MARSHAL 负例矩阵 ----
        check(org.omg.PortableServer.ServantActivatorHelper.id().equals("IDL:omg.org/PortableServer/ServantActivator:2.3"), "id exact (repo id version 2.3)");
        check(org.omg.PortableServer.ServantActivatorHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref, "type().kind() == tk_objref");
        check(org.omg.PortableServer.ServantActivatorHelper.type().id().equals(org.omg.PortableServer.ServantActivatorHelper.id()), "type().id() == id()");
        check(org.omg.PortableServer.ServantActivatorHelper.type().name().equals("ServantActivator"), "type().name() == ServantActivator");
        check(org.omg.PortableServer.ServantActivatorHelper.unchecked_narrow(null) == null, "unchecked_narrow(null) == null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        try {
            org.omg.PortableServer.ServantActivatorHelper.read(orb.create_output_stream().create_input_stream());
            check(false, "read of empty stream must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "read(empty stream) -> MARSHAL minor==0");
        }
        try {
            org.omg.PortableServer.ServantActivatorHelper.extract(orb.create_any());
            check(false, "extract of empty Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "extract(empty Any) -> MARSHAL minor==0");
        }
        try {
            org.omg.PortableServer.ServantActivatorHelper.write(orb.create_output_stream(), null);
            check(false, "write of null objref must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0,
                  "write(null) -> MARSHAL minor==0 (helper rejects null; raw write_Object(null) is legal)");
        }
        try {
            org.omg.PortableServer.ServantActivatorHelper.insert(orb.create_any(), null);
            check(false, "insert of null objref must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "insert(any, null) -> MARSHAL minor==0");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
