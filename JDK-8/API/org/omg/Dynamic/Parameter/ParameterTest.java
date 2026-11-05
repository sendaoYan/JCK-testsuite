/*
 * @test
 * @summary org.omg.Dynamic.Parameter conformance: value type fields and constructors.
 * @run main ParameterTest
 */

public class ParameterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.Dynamic.Parameter s0 = new org.omg.Dynamic.Parameter();
        check(s0.argument == null, "no-arg argument default");
        check(s0.mode == null, "no-arg mode default");
        org.omg.Dynamic.Parameter s1 = new org.omg.Dynamic.Parameter(null, null);
        // ---- deepened (batch C, jdk8u372 probed): struct fields + mode matrix ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.Dynamic.Parameter.class.getModifiers()), "Parameter is final (IDL struct)");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.Dynamic.Parameter.class), "implements IDLEntity");
        check(java.io.Serializable.class.isAssignableFrom(org.omg.Dynamic.Parameter.class), "Serializable");
        org.omg.Dynamic.Parameter dflt = new org.omg.Dynamic.Parameter();
        check(dflt.argument == null && dflt.mode == null, "default ctor leaves argument and mode null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.Any dpAny = orb.create_any();
        dpAny.insert_string("zz");
        org.omg.Dynamic.Parameter full = new org.omg.Dynamic.Parameter(dpAny, org.omg.CORBA.ParameterMode.PARAM_IN);
        check(full.argument == dpAny, "ctor stores the Any reference verbatim (no copy)");
        check(full.argument.extract_string().equals("zz"), "argument content readback");
        check(full.mode == org.omg.CORBA.ParameterMode.PARAM_IN, "mode constant readback");
        org.omg.Dynamic.Parameter selfEq = full;
        check(full.equals(selfEq), "equals identity only (no value-equals override)");
        check(!full.equals(new org.omg.Dynamic.Parameter(dpAny, org.omg.CORBA.ParameterMode.PARAM_IN)),
              "structurally identical distinct instance NOT equal (identity semantics)");
        check(!full.equals(null), "equals(null) false");
        check(full.hashCode() == System.identityHashCode(full), "hashCode not overridden");
        check(full.toString().startsWith("org.omg.Dynamic.Parameter@"), "toString default Object pattern");
        org.omg.Dynamic.Parameter mut = new org.omg.Dynamic.Parameter();
        org.omg.CORBA.Any otherAny = orb.create_any();
        otherAny.insert_long(7);
        mut.argument = otherAny;
        mut.mode = org.omg.CORBA.ParameterMode.PARAM_OUT;
        check(mut.argument.extract_long() == 7 && mut.mode == org.omg.CORBA.ParameterMode.PARAM_OUT, "public fields mutable");
        mut.argument = null;
        mut.mode = null;
        check(mut.argument == null && mut.mode == null, "fields resettable to null");
        // ParameterMode constants (org.omg.CORBA) cross-check: exact value table + from_int normalization + boundary
        check(org.omg.CORBA.ParameterMode._PARAM_IN == 0 && org.omg.CORBA.ParameterMode._PARAM_OUT == 1
              && org.omg.CORBA.ParameterMode._PARAM_INOUT == 2, "ParameterMode int constants {0,1,2}");
        check(org.omg.CORBA.ParameterMode.PARAM_INOUT.value() == 2, "PARAM_INOUT.value()==2");
        check(org.omg.CORBA.ParameterMode.from_int(1) == org.omg.CORBA.ParameterMode.PARAM_OUT, "from_int(1) normalizes to PARAM_OUT");
        try {
            org.omg.CORBA.ParameterMode.from_int(9);
            throw new AssertionError("from_int(9) must throw");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "ParameterMode.from_int(9) -> org.omg.CORBA.BAD_PARAM");
        }
        // all three modes usable as field values
        for (org.omg.CORBA.ParameterMode m : new org.omg.CORBA.ParameterMode[]{
                org.omg.CORBA.ParameterMode.PARAM_IN, org.omg.CORBA.ParameterMode.PARAM_OUT, org.omg.CORBA.ParameterMode.PARAM_INOUT}) {
            org.omg.Dynamic.Parameter pm = new org.omg.Dynamic.Parameter(dpAny, m);
            check(pm.mode == m && pm.mode.value() == m.value(), "each ParameterMode stored and read back (value " + m.value() + ")");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
