/*
 * @test
 * @summary org.omg.DynamicAny.NameDynAnyPair conformance: value type fields and constructors.
 * @run main NameDynAnyPairTest
 */

public class NameDynAnyPairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.DynamicAny.NameDynAnyPair s0 = new org.omg.DynamicAny.NameDynAnyPair();
        check(s0.id == null, "no-arg id default");
        check(s0.value == null, "no-arg value default");
        org.omg.DynamicAny.NameDynAnyPair s1 = new org.omg.DynamicAny.NameDynAnyPair("s", null);
        check(s1.id == "s", "ctor id stored");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): struct fields + identity semantics ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.DynamicAny.NameDynAnyPair.class.getModifiers()), "final struct");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.DynamicAny.NameDynAnyPair.class), "IDLEntity");
        org.omg.DynamicAny.NameDynAnyPair dpN0 = new org.omg.DynamicAny.NameDynAnyPair();
        check(dpN0.id == null && dpN0.value == null, "default ctor leaves id and value null");
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        org.omg.DynamicAny.DynAny dpD = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        org.omg.DynamicAny.NameDynAnyPair dpN1 = new org.omg.DynamicAny.NameDynAnyPair("m", dpD);
        check(dpN1.id.equals("m"), "ctor id readback");
        check(dpN1.value == dpD, "ctor stores the DynAny reference verbatim");
        check(dpN1.equals(dpN1), "equals identity only");
        check(!dpN1.equals(new org.omg.DynamicAny.NameDynAnyPair("m", dpD)), "identity semantics (no value equals)");
        check(!dpN1.equals(null), "equals(null) false");
        check(dpN1.hashCode() == System.identityHashCode(dpN1), "hashCode not overridden");
        dpN1.value.component_count();
        check(dpN1.value == dpD, "field still references the live DynAny (no defensive copy)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
