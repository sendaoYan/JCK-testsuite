/*
 * @test
 * @summary org.omg.DynamicAny.DynSequence conformance: interface shape.
 * @run main DynSequenceTest
 */

public class DynSequenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynSequence");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynSequenceOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynSequenceOperations");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAny");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        // ---- deepened (batch C, jdk8u372 probed): unbounded sequence length/elements lifecycle ----
        org.omg.CORBA.TypeCode dpLtc = orb.create_sequence_tc(0, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        org.omg.DynamicAny.DynSequence dpSq = (org.omg.DynamicAny.DynSequence) dpFac.create_dyn_any_from_type_code(dpLtc);
        check(dpSq.get_length() == 0, "fresh unbounded sequence length 0");
        dpSq.set_length(2);
        check(dpSq.get_length() == 2, "set_length(2) readback");
        org.omg.CORBA.Any dpE1 = orb.create_any(); dpE1.insert_long(10);
        org.omg.CORBA.Any dpE2 = orb.create_any(); dpE2.insert_long(20);
        dpSq.set_elements(new org.omg.CORBA.Any[]{dpE1, dpE2});
        org.omg.CORBA.Any[] dpEls = dpSq.get_elements();
        check(dpEls.length == 2, "get_elements length 2");
        check(dpEls[0].extract_long() == 10 && dpEls[1].extract_long() == 20, "elements values round-trip");
        org.omg.DynamicAny.DynAny[] dpDas = dpSq.get_elements_as_dyn_any();
        check(dpDas.length == 2 && dpDas[0].get_long() == 10, "get_elements_as_dyn_any values round-trip");
        dpSq.set_elements_as_dyn_any(dpDas);
        check(dpSq.get_elements()[1].extract_long() == 20, "set_elements_as_dyn_any round-trip");
        dpSq.set_length(0);
        check(dpSq.get_length() == 0 && dpSq.get_elements().length == 0, "shrink to 0 works (length 0, zero-length elements array)");
        dpSq.set_length(2);
        dpSq.set_elements(new org.omg.CORBA.Any[]{dpE1, dpE2});
        check(dpSq.get_length() == 2 && dpSq.get_elements()[0].extract_long() == 10, "re-grow to 2 and refill works");
        try {
            dpSq.set_length(-1);
            throw new AssertionError("set_length(-1) must throw");
        } catch (java.lang.NegativeArraySizeException expected) {
            check(true, "set_length(-1) -> java.lang.NegativeArraySizeException (JDK-8 implementation throws unchecked, NOT InvalidValue)");
        }
        try {
            dpSq.set_elements(null);
            throw new AssertionError("set_elements(null) must throw");
        } catch (java.lang.NullPointerException expected) {
            check(true, "set_elements(null) -> NullPointerException (JDK-8 implementation detail)");
        }
        try {
            org.omg.CORBA.Any dpStr = orb.create_any(); dpStr.insert_string("s");
            dpSq.set_elements(new org.omg.CORBA.Any[]{dpStr});
            throw new AssertionError("wrong element type must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "set_elements(string into long sequence) -> DynAnyPackage.TypeMismatch");
        }
        check(dpSq.get_length() == 0,
              "recorded JDK-8 quirk: failed set_elements(TypeMismatch) collapses reported length 2->0 (internal components destroyed)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
