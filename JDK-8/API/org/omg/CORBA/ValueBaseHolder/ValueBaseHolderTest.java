/*
 * @test
 * @summary org.omg.CORBA.ValueBaseHolder conformance: holder value field.
 * @run main ValueBaseHolderTest
 */

public class ValueBaseHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ValueBaseHolder h0 = new org.omg.CORBA.ValueBaseHolder();
        check(h0.value == null, "no-arg default value");
        java.io.Serializable v = null;
        org.omg.CORBA.ValueBaseHolder h1 = new org.omg.CORBA.ValueBaseHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.ValueBaseHolder().value == null, "default value is null");
        check("strv".equals(new org.omg.CORBA.ValueBaseHolder("strv").value), "ctor stores Serializable value");
        check(new org.omg.CORBA.ValueBaseHolder()._type().kind() == org.omg.CORBA.TCKind.tk_value,
              "ValueBaseHolder._type().kind() == tk_value");
        org.omg.CORBA.portable.OutputStream osVb = orb.create_output_stream();
        new org.omg.CORBA.ValueBaseHolder("strv")._write(osVb);
        org.omg.CORBA.ValueBaseHolder whVb = new org.omg.CORBA.ValueBaseHolder();
        whVb._read(osVb.create_input_stream());
        check("strv".equals(whVb.value), "CDR wire round-trip (String value)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
