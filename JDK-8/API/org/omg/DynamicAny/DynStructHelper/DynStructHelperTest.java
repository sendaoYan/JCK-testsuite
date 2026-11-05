/*
 * @test
 * @summary org.omg.DynamicAny.DynStructHelper conformance: helper static id/type/narrow.
 * @run main DynStructHelperTest
 */

public class DynStructHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynStructHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynStructHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynStructHelper.narrow(null) == null, "narrow(null) returns null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        // ---- deepened (batch C, jdk8u372 probed): objref helper identity ----
        check(org.omg.DynamicAny.DynStructHelper.id().equals("IDL:omg.org/DynamicAny/DynStruct:1.0"), "id() exact");
        check(org.omg.DynamicAny.DynStructHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref, "type() tk_objref");
        check(org.omg.DynamicAny.DynStructHelper.narrow(null) == null, "narrow(null) -> null");
        org.omg.CORBA.Any dpNil = orb.create_any();
        try {
            org.omg.DynamicAny.DynStructHelper.insert(dpNil, null);
            throw new AssertionError("insert(nil) expected MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "insert(nil) -> org.omg.CORBA.MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
