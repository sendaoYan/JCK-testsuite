/*
 * @test
 * @summary org.omg.DynamicAny.DynArray conformance: interface shape.
 * @run main DynArrayTest
 */

public class DynArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynArray");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynArrayOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynArrayOperations");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAny");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        // ---- deepened (batch C, jdk8u372 probed): fixed-length array element model ----
        org.omg.CORBA.TypeCode dpAtc = orb.create_array_tc(2, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        org.omg.DynamicAny.DynArray dpAr = (org.omg.DynamicAny.DynArray) dpFac.create_dyn_any_from_type_code(dpAtc);
        check(dpAr.get_elements().length == 2, "array element count fixed at 2");
        org.omg.CORBA.Any dpA1 = orb.create_any(); dpA1.insert_long(10);
        org.omg.CORBA.Any dpA2 = orb.create_any(); dpA2.insert_long(20);
        dpAr.set_elements(new org.omg.CORBA.Any[]{dpA1, dpA2});
        org.omg.CORBA.Any[] dpAel = dpAr.get_elements();
        check(dpAel[0].extract_long() == 10 && dpAel[1].extract_long() == 20, "set/get_elements value round-trip");
        org.omg.DynamicAny.DynAny[] dpAda = dpAr.get_elements_as_dyn_any();
        check(dpAda.length == 2 && dpAda[0].get_long() == 10, "get_elements_as_dyn_any values round-trip");
        dpAr.set_elements_as_dyn_any(dpAda);
        check(dpAr.get_elements()[1].extract_long() == 20, "set_elements_as_dyn_any round-trip");
        check(dpAr.component_count() == 2, "array component_count()==2 (fixed length)");
        try {
            dpAr.set_elements(new org.omg.CORBA.Any[]{dpA1});
            throw new AssertionError("wrong element count must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue expected) {
            check(true, "set_elements(length 1 into array of 2) -> DynAnyPackage.InvalidValue");
        }
        check(dpAr.component_count() == 2, "component_count stays 2 after InvalidValue (count-only failure is atomic)");
        try {
            org.omg.CORBA.Any dpStr = orb.create_any(); dpStr.insert_string("s");
            dpAr.set_elements(new org.omg.CORBA.Any[]{dpStr, dpStr});
            throw new AssertionError("wrong element type must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "set_elements(string elements) -> TypeMismatch");
        }
        check(dpAr.component_count() == 0,
              "recorded JDK-8 quirk: failed set_elements(TypeMismatch) destroys components (component_count 2->0)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
