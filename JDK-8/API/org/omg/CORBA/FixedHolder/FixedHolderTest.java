/*
 * @test
 * @summary org.omg.CORBA.FixedHolder conformance: holder value field.
 * @run main FixedHolderTest
 */

public class FixedHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.FixedHolder h0 = new org.omg.CORBA.FixedHolder();
        check(h0.value == null, "no-arg default value");
        java.math.BigDecimal v = new java.math.BigDecimal("1.5");
        org.omg.CORBA.FixedHolder h1 = new org.omg.CORBA.FixedHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.FixedHolder.class),
              "FixedHolder implements org.omg.CORBA.portable.Streamable");
        check(new org.omg.CORBA.FixedHolder().value == null,
              "default value is null (reference type BigDecimal, NOT zero)");
        java.math.BigDecimal bd = new java.math.BigDecimal("123.45");
        check(new org.omg.CORBA.FixedHolder(bd).value == bd, "ctor stores same BigDecimal reference");
        check(new org.omg.CORBA.FixedHolder()._type().kind() == org.omg.CORBA.TCKind.tk_fixed,
              "FixedHolder._type().kind() == tk_fixed");
        check(new org.omg.CORBA.FixedHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_fixed)),
              "FixedHolder._type().equal(get_primitive_tc(tk_fixed))");
        org.omg.CORBA.portable.OutputStream osFx = orb.create_output_stream();
        new org.omg.CORBA.FixedHolder(bd)._write(osFx);
        org.omg.CORBA.FixedHolder whFx = new org.omg.CORBA.FixedHolder();
        whFx._read(osFx.create_input_stream());
        check(whFx.value.compareTo(new java.math.BigDecimal("12345")) == 0 && whFx.value.scale() == 0,
              "CDR fixed wire drops the scale: 123.45 round-trips as unscaled 12345 scale 0");
        org.omg.CORBA.portable.OutputStream osF7 = orb.create_output_stream();
        new org.omg.CORBA.FixedHolder(new java.math.BigDecimal("7"))._write(osF7);
        org.omg.CORBA.FixedHolder whF7 = new org.omg.CORBA.FixedHolder();
        whF7._read(osF7.create_input_stream());
        check(whF7.value.compareTo(new java.math.BigDecimal("7")) == 0, "integer fixed wire round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
