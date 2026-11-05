/*
 * @test
 * @summary org.omg.DynamicAny.DynValueHelper conformance: helper static id/type/narrow.
 * @run main DynValueHelperTest
 */

public class DynValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynValueHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynValueHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynValueHelper.narrow(null) == null, "narrow(null) returns null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): objref helper identity ----
        check(org.omg.DynamicAny.DynValueHelper.id().equals("IDL:omg.org/DynamicAny/DynValue:1.0"), "id() exact");
        check(org.omg.DynamicAny.DynValueHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref, "type() tk_objref");
        check(org.omg.DynamicAny.DynValueHelper.type().name().equals("DynValue"), "type().name() DynValue");
        check(org.omg.DynamicAny.DynValueHelper.narrow(null) == null, "narrow(null) -> null");
        org.omg.CORBA.Any dpNil = orb.create_any();
        try {
            org.omg.DynamicAny.DynValueHelper.insert(dpNil, null);
            throw new AssertionError("insert(nil) expected MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "insert(nil) -> org.omg.CORBA.MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
