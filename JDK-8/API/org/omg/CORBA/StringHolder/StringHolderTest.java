/*
 * @test
 * @summary org.omg.CORBA.StringHolder conformance: holder value field.
 * @run main StringHolderTest
 */

public class StringHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.StringHolder h0 = new org.omg.CORBA.StringHolder();
        check(h0.value == null, "no-arg default value");
        java.lang.String v = "s";
        org.omg.CORBA.StringHolder h1 = new org.omg.CORBA.StringHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.StringHolder.class),
              "StringHolder implements org.omg.CORBA.portable.Streamable");
        check("hi".equals(new org.omg.CORBA.StringHolder("hi").value), "ctor stores hi");
        org.omg.CORBA.StringHolder whSt = new org.omg.CORBA.StringHolder("a");
        whSt.value = "b";
        check("b".equals(whSt.value), "value field is mutable and reads back");
        check(new org.omg.CORBA.StringHolder()._type().kind() == org.omg.CORBA.TCKind.tk_string,
              "StringHolder._type().kind() == tk_string");
        check(new org.omg.CORBA.StringHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string)),
              "StringHolder._type().equal(get_primitive_tc(tk_string))");
        org.omg.CORBA.portable.OutputStream osW14 = orb.create_output_stream();
        new org.omg.CORBA.StringHolder("hi")._write(osW14);
        org.omg.CORBA.StringHolder whRt14 = new org.omg.CORBA.StringHolder();
        whRt14._read(osW14.create_input_stream());
        check("hi".equals(whRt14.value), "StringHolder CDR wire round-trip value hi");
        org.omg.CORBA.portable.OutputStream osW15 = orb.create_output_stream();
        new org.omg.CORBA.StringHolder("")._write(osW15);
        org.omg.CORBA.StringHolder whRt15 = new org.omg.CORBA.StringHolder();
        whRt15._read(osW15.create_input_stream());
        check("".equals(whRt15.value), "StringHolder CDR wire round-trip empty string");
        org.omg.CORBA.portable.OutputStream osW16 = orb.create_output_stream();
        new org.omg.CORBA.StringHolder("a\nb")._write(osW16);
        org.omg.CORBA.StringHolder whRt16 = new org.omg.CORBA.StringHolder();
        whRt16._read(osW16.create_input_stream());
        check("a\nb".equals(whRt16.value), "StringHolder CDR wire round-trip string containing newline");
        try {
            org.omg.CORBA.portable.OutputStream osSn = orb.create_output_stream(); new org.omg.CORBA.StringHolder(null)._write(osSn);
            throw new AssertionError("StringHolder(null)._write should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "StringHolder(null)._write -> BAD_PARAM");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
