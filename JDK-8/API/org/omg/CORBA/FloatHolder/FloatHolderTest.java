/*
 * @test
 * @summary org.omg.CORBA.FloatHolder conformance: holder value field.
 * @run main FloatHolderTest
 */

public class FloatHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.FloatHolder h0 = new org.omg.CORBA.FloatHolder();
        check(h0.value == 0.0f, "no-arg default value");
        float v = 7.0f;
        org.omg.CORBA.FloatHolder h1 = new org.omg.CORBA.FloatHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.FloatHolder.class),
              "FloatHolder implements org.omg.CORBA.portable.Streamable");
        check(new org.omg.CORBA.FloatHolder(Float.MIN_VALUE).value == Float.MIN_VALUE, "ctor stores Float.MIN_VALUE");
        check(new org.omg.CORBA.FloatHolder(Float.NaN).value != new org.omg.CORBA.FloatHolder(Float.NaN).value,
              "ctor stores NaN (self-inequality proves NaN payload)");
        check(new org.omg.CORBA.FloatHolder()._type().kind() == org.omg.CORBA.TCKind.tk_float,
              "FloatHolder._type().kind() == tk_float");
        check(new org.omg.CORBA.FloatHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_float)),
              "FloatHolder._type().equal(get_primitive_tc(tk_float))");
        org.omg.CORBA.portable.OutputStream osW8 = orb.create_output_stream();
        new org.omg.CORBA.FloatHolder(1.5f)._write(osW8);
        org.omg.CORBA.FloatHolder whRt8 = new org.omg.CORBA.FloatHolder();
        whRt8._read(osW8.create_input_stream());
        check(whRt8.value == 1.5f, "FloatHolder CDR wire round-trip value 1.5f");
        org.omg.CORBA.portable.OutputStream osFn = orb.create_output_stream();
        new org.omg.CORBA.FloatHolder(Float.NaN)._write(osFn);
        org.omg.CORBA.FloatHolder whFn = new org.omg.CORBA.FloatHolder();
        whFn._read(osFn.create_input_stream());
        check(java.lang.Float.isNaN(whFn.value), "FloatHolder NaN CDR wire round-trip");
        org.omg.CORBA.Any anyF = orb.create_any();
        anyF.insert_float(0.25f);
        org.omg.CORBA.FloatHolder whFA = new org.omg.CORBA.FloatHolder();
        whFA._read(anyF.create_input_stream());
        check(whFA.value == 0.25f, "Any channel: insert_float -> holder._read == 0.25f");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
