/*
 * @test
 * @summary org.omg.PortableServer.CurrentHelper conformance: helper static id/type/narrow.
 * @run main CurrentHelperTest
 */

public class CurrentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.CurrentHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.CurrentHelper.type() != null, "type() non-null");
        check(org.omg.PortableServer.CurrentHelper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened (jdk8u372-b07 probed): repo id 版本 / tk_objref / narrow null / MARSHAL 负例矩阵 ----
        check(org.omg.PortableServer.CurrentHelper.id().equals("IDL:omg.org/PortableServer/Current:2.3"), "id exact (repo id version 2.3)");
        check(org.omg.PortableServer.CurrentHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref, "type().kind() == tk_objref");
        check(org.omg.PortableServer.CurrentHelper.type().id().equals(org.omg.PortableServer.CurrentHelper.id()), "type().id() == id()");
        check(org.omg.PortableServer.CurrentHelper.type().name().equals("Current"), "type().name() == Current");
        try {
            org.omg.PortableServer.CurrentHelper.class.getMethod("unchecked_narrow", org.omg.CORBA.Object.class);
            check(false, "unexpected: CurrentHelper declares unchecked_narrow");
        } catch (NoSuchMethodException expected) {
            check(true, "old-mapping trap: CurrentHelper has narrow() but NO unchecked_narrow");
        }
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        try {
            org.omg.PortableServer.CurrentHelper.read(orb.create_output_stream().create_input_stream());
            check(false, "read of empty stream must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "read(empty stream) -> MARSHAL minor==0");
        }
        try {
            org.omg.PortableServer.CurrentHelper.extract(orb.create_any());
            check(false, "extract of empty Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "extract(empty Any) -> MARSHAL minor==0");
        }
        try {
            org.omg.PortableServer.CurrentHelper.write(orb.create_output_stream(), null);
            check(false, "write of null objref must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0,
                  "write(null) -> MARSHAL minor==0 (helper rejects null; raw write_Object(null) is legal)");
        }
        try {
            org.omg.PortableServer.CurrentHelper.insert(orb.create_any(), null);
            check(false, "insert of null objref must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(expected.minor == 0, "insert(any, null) -> MARSHAL minor==0");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
