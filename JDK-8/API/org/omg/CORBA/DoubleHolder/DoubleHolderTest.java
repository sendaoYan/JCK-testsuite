/*
 * @test
 * @summary org.omg.CORBA.DoubleHolder conformance: holder value field.
 * @run main DoubleHolderTest
 */

public class DoubleHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.DoubleHolder h0 = new org.omg.CORBA.DoubleHolder();
        check(h0.value == 0.0, "no-arg default value");
        double v = 7.0;
        org.omg.CORBA.DoubleHolder h1 = new org.omg.CORBA.DoubleHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.DoubleHolder.class),
              "DoubleHolder implements org.omg.CORBA.portable.Streamable");
        check(new org.omg.CORBA.DoubleHolder(Double.MIN_VALUE).value == Double.MIN_VALUE, "ctor stores Double.MIN_VALUE");
        check(new org.omg.CORBA.DoubleHolder(Double.NaN).value != new org.omg.CORBA.DoubleHolder(Double.NaN).value,
              "ctor stores NaN (self-inequality proves NaN payload)");
        check(new org.omg.CORBA.DoubleHolder()._type().kind() == org.omg.CORBA.TCKind.tk_double,
              "DoubleHolder._type().kind() == tk_double");
        check(new org.omg.CORBA.DoubleHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_double)),
              "DoubleHolder._type().equal(get_primitive_tc(tk_double))");
        org.omg.CORBA.portable.OutputStream osW7 = orb.create_output_stream();
        new org.omg.CORBA.DoubleHolder(3.25)._write(osW7);
        org.omg.CORBA.DoubleHolder whRt7 = new org.omg.CORBA.DoubleHolder();
        whRt7._read(osW7.create_input_stream());
        check(whRt7.value == 3.25, "DoubleHolder CDR wire round-trip value 3.25");
        org.omg.CORBA.portable.OutputStream osNaN = orb.create_output_stream();
        new org.omg.CORBA.DoubleHolder(Double.NaN)._write(osNaN);
        org.omg.CORBA.DoubleHolder whNaN = new org.omg.CORBA.DoubleHolder();
        whNaN._read(osNaN.create_input_stream());
        check(java.lang.Double.isNaN(whNaN.value), "DoubleHolder NaN CDR wire round-trip");
        org.omg.CORBA.portable.OutputStream osInf = orb.create_output_stream();
        new org.omg.CORBA.DoubleHolder(Double.POSITIVE_INFINITY)._write(osInf);
        org.omg.CORBA.DoubleHolder whInf = new org.omg.CORBA.DoubleHolder();
        whInf._read(osInf.create_input_stream());
        check(whInf.value == Double.POSITIVE_INFINITY, "DoubleHolder +Inf CDR wire round-trip");
        org.omg.CORBA.Any anyD = orb.create_any();
        anyD.insert_double(6.5);
        org.omg.CORBA.DoubleHolder whDA = new org.omg.CORBA.DoubleHolder();
        whDA._read(anyD.create_input_stream());
        check(whDA.value == 6.5, "Any channel: insert_double -> holder._read == 6.5");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
