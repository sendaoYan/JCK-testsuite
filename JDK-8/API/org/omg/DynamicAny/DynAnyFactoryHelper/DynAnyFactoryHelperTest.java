/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyFactoryHelper conformance: helper static id/type/narrow.
 * @run main DynAnyFactoryHelperTest
 */

public class DynAnyFactoryHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyFactoryHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyFactoryHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynAnyFactoryHelper.narrow(null) == null, "narrow(null) returns null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        check(dpFac != null, "resolve_initial_references(DynAnyFactory) + narrow works offline");
        // ---- deepened (batch C, jdk8u372 probed): id/type + narrow channels ----
        check(org.omg.DynamicAny.DynAnyFactoryHelper.id().equals("IDL:omg.org/DynamicAny/DynAnyFactory:1.0"), "id() exact");
        check(org.omg.DynamicAny.DynAnyFactoryHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref, "type() tk_objref");
        check(org.omg.DynamicAny.DynAnyFactoryHelper.type().id().equals("IDL:omg.org/DynamicAny/DynAnyFactory:1.0"), "type().id() exact");
        check(org.omg.DynamicAny.DynAnyFactoryHelper.narrow(null) == null, "narrow(null) -> null");
        org.omg.CORBA.Any dpAnyNull = orb.create_any();
        try {
            org.omg.DynamicAny.DynAnyFactoryHelper.insert(dpAnyNull, null);
            throw new AssertionError("insert(null) expected MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "insert(nil factory) -> org.omg.CORBA.MARSHAL (DynamicAny objref helpers reject nil here, unlike CosNaming ones)");
        }
        // the factory obtained via resolve_initial_references narrows to itself
        check(org.omg.DynamicAny.DynAnyFactoryHelper.narrow((org.omg.CORBA.Object) dpFac) == dpFac,
              "narrow of the live factory returns the same object");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
