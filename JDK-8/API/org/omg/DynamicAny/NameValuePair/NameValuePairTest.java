/*
 * @test
 * @summary org.omg.DynamicAny.NameValuePair conformance: value type fields and constructors.
 * @run main NameValuePairTest
 */

public class NameValuePairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.DynamicAny.NameValuePair s0 = new org.omg.DynamicAny.NameValuePair();
        check(s0.id == null, "no-arg id default");
        check(s0.value == null, "no-arg value default");
        org.omg.DynamicAny.NameValuePair s1 = new org.omg.DynamicAny.NameValuePair("s", null);
        check(s1.id == "s", "ctor id stored");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): struct fields + identity semantics ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.DynamicAny.NameValuePair.class.getModifiers()), "final struct");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.DynamicAny.NameValuePair.class), "IDLEntity");
        check(java.io.Serializable.class.isAssignableFrom(org.omg.DynamicAny.NameValuePair.class), "Serializable");
        org.omg.DynamicAny.NameValuePair dpN0 = new org.omg.DynamicAny.NameValuePair();
        check(dpN0.id == null && dpN0.value == null, "default ctor leaves id and value null");
        org.omg.CORBA.Any dpVa = orb.create_any(); dpVa.insert_long(5);
        org.omg.DynamicAny.NameValuePair dpN1 = new org.omg.DynamicAny.NameValuePair("n", dpVa);
        check(dpN1.id.equals("n"), "ctor id readback");
        check(dpN1.value == dpVa, "ctor stores the Any reference verbatim (no copy)");
        check(dpN1.value.extract_long() == 5, "value content readback");
        check(dpN1.equals(dpN1), "equals identity only");
        check(!dpN1.equals(new org.omg.DynamicAny.NameValuePair("n", dpVa)), "structurally identical distinct instance NOT equal");
        check(!dpN1.equals(null), "equals(null) false");
        check(dpN1.hashCode() == System.identityHashCode(dpN1), "hashCode not overridden");
        dpN1.id = "other";
        dpN1.value = null;
        check(dpN1.id.equals("other") && dpN1.value == null, "public fields mutable / nullable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
