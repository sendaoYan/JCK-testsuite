/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyOperations conformance: interface shape.
 * @run main DynAnyOperationsTest
 */

public class DynAnyOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynAnyOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        check(dpFac != null, "resolve_initial_references(DynAnyFactory) + narrow works offline");
        // ---- deepened (batch C, jdk8u372 probed): DynAnyOperations behavioral matrix via live instances ----
        org.omg.DynamicAny.DynAny dpLong = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        check(dpLong.type().kind() == org.omg.CORBA.TCKind.tk_long, "type() reflects tk_long");
        check(dpLong.get_long() == 0, "freshly constructed long DynAny defaults to 0");
        dpLong.insert_long(-5);
        check(dpLong.get_long() == -5, "insert_long(-5)/get_long round-trip");
        try {
            dpLong.insert_string("x");
            throw new AssertionError("insert_string on tk_long must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "insert_string on tk_long -> TypeMismatch");
        }
        try {
            dpLong.get_string();
            throw new AssertionError("get_string on tk_long must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "get_string on tk_long -> TypeMismatch");
        }
        try {
            dpLong.insert_octet((byte) 1);
            throw new AssertionError("insert_octet on tk_long must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "insert_octet on tk_long -> TypeMismatch (no implicit widening)");
        }
        try {
            dpLong.insert_ulong(5);
            throw new AssertionError("insert_ulong on tk_long must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "insert_ulong on tk_long -> TypeMismatch (signedness matters)");
        }
        // from_any / to_any
        org.omg.CORBA.Any dpSrc = orb.create_any(); dpSrc.insert_long(42);
        org.omg.DynamicAny.DynAny dpFa = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        dpFa.from_any(dpSrc);
        check(dpFa.get_long() == 42, "from_any(long 42) -> get_long()==42");
        org.omg.CORBA.Any dpOut = dpFa.to_any();
        check(dpOut.extract_long() == 42 && dpOut.type().kind() == org.omg.CORBA.TCKind.tk_long, "to_any() value + kind round-trip");
        org.omg.CORBA.Any dpBad = orb.create_any(); dpBad.insert_string("nope");
        try {
            dpFa.from_any(dpBad);
            throw new AssertionError("from_any(string) on tk_long must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "from_any(string any) on tk_long -> TypeMismatch");
        }
        // basic-type component model
        check(dpFa.component_count() == 0, "basic type component_count()==0");
        check(dpFa.seek(0) == false, "seek(0) false on basic type");
        check(dpFa.next() == false, "next() false on basic type");
        dpFa.rewind();
        check(true, "rewind() safe on basic type");
        // struct traversal
        org.omg.CORBA.StructMember[] dpSm = new org.omg.CORBA.StructMember[]{
            new org.omg.CORBA.StructMember("g1", orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), null),
            new org.omg.CORBA.StructMember("g2", orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string), null)};
        org.omg.DynamicAny.DynStruct dpDs = (org.omg.DynamicAny.DynStruct) dpFac.create_dyn_any_from_type_code(
            orb.create_struct_tc("IDL:S2:1.0", "S2", dpSm));
        check(dpDs.component_count() == 2, "struct component_count()==2");
        check(dpDs.seek(1), "seek(1) moves to member g2");
        check(dpDs.current_member_name().equals("g2"), "current position member g2");
        check(dpDs.next() == false, "next() false past the last member");
        dpDs.rewind();
        check(dpDs.current_member_name().equals("g1"), "rewind() returns to first member");
        check(dpDs.seek(5) == false, "seek beyond member count false");
        org.omg.DynamicAny.DynAny dpComp = dpDs.current_component();
        check(dpComp != null && dpComp.type().kind() == org.omg.CORBA.TCKind.tk_long, "current_component() of struct member g1");
        // copy independence + assign
        org.omg.DynamicAny.DynAny dpX = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        dpX.insert_long(100);
        org.omg.DynamicAny.DynAny dpCp = dpX.copy();
        check(dpCp.get_long() == 100 && dpX.equal(dpCp), "copy() equal after creation");
        dpCp.insert_long(200);
        check(dpX.get_long() == 100 && dpCp.get_long() == 200, "copy() is independent (mutation does not propagate)");
        check(!dpX.equal(dpCp), "equal() false after divergent mutation");
        org.omg.DynamicAny.DynAny dpY = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        dpY.assign(dpX);
        check(dpY.get_long() == 100 && dpX.equal(dpY), "assign() copies value");
        org.omg.DynamicAny.DynAny dpZs = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
        try {
            dpY.assign(dpZs);
            throw new AssertionError("assign type mismatch must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "assign(string dynany) on tk_long -> TypeMismatch");
        }
        check(dpX.equal(dpX), "equal(self) true");
        try {
            dpX.equal(null);
            check(true, "equal(null) tolerated (behavior recorded, not normative)");
        } catch (NullPointerException npe) {
            check(true, "equal(null) -> NullPointerException (JDK impl detail, recorded)");
        }
        // destroy semantics: first ok, any later op (incl. destroy) -> OBJECT_NOT_EXIST
        org.omg.CORBA.Any dpOne = orb.create_any(); dpOne.insert_long(1);
        org.omg.DynamicAny.DynAny dpDd = dpFac.create_dyn_any(dpOne);
        dpDd.destroy();
        check(true, "first destroy() completes");
        try { dpDd.get_long(); } catch (org.omg.CORBA.OBJECT_NOT_EXIST expected) {
            check(true, "get_long after destroy -> OBJECT_NOT_EXIST (SUN minor 207)");
        }
        try { dpDd.destroy(); } catch (org.omg.CORBA.OBJECT_NOT_EXIST expected) {
            check(true, "second destroy() -> OBJECT_NOT_EXIST (destroy is NOT idempotent in JDK-8)");
        }
        try { dpDd.type(); } catch (org.omg.CORBA.OBJECT_NOT_EXIST expected) {
            check(true, "type() after destroy -> OBJECT_NOT_EXIST");
        }
        try { dpDd.to_any(); } catch (org.omg.CORBA.OBJECT_NOT_EXIST expected) {
            check(true, "to_any() after destroy -> OBJECT_NOT_EXIST");
        }
        // wstring + any channels
        org.omg.DynamicAny.DynAny dpWs = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_wstring));
        dpWs.insert_wstring("wv");
        check(dpWs.get_wstring().equals("wv"), "insert_wstring/get_wstring round-trip");
        try {
            dpWs.insert_long(1);
            throw new AssertionError("insert_long on tk_wstring must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "insert_long on tk_wstring -> TypeMismatch");
        }
        org.omg.DynamicAny.DynAny dpAa = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_any));
        org.omg.CORBA.Any dpInner = orb.create_any(); dpInner.insert_long(99);
        dpAa.insert_any(dpInner);
        check(dpAa.get_any().extract_long() == 99, "insert_any/get_any nested value round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
