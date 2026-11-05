/*
 * @test
 * @summary org.omg.CORBA.ValueBaseHelper conformance: helper static id/type/narrow.
 * @run main ValueBaseHelperTest
 */

public class ValueBaseHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ValueBaseHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ValueBaseHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ValueBaseHelper.id().equals("IDL:omg.org/CORBA/ValueBase:1.0"),
              "ValueBaseHelper.id() exact repository id");
        check(org.omg.CORBA.ValueBaseHelper.type().kind() == org.omg.CORBA.TCKind.tk_value,
              "ValueBaseHelper.type().kind() == tk_value");
        org.omg.CORBA.portable.OutputStream osVb = orb.create_output_stream();
        org.omg.CORBA.ValueBaseHelper.write(osVb, "vb");
        check("vb".equals(org.omg.CORBA.ValueBaseHelper.read(osVb.create_input_stream())),
              "write/read CDR round-trip (String as java.io.Serializable value)");
        org.omg.CORBA.Any anyVb = orb.create_any();
        org.omg.CORBA.ValueBaseHelper.insert(anyVb, "vb2");
        check("vb2".equals(org.omg.CORBA.ValueBaseHelper.extract(anyVb)), "insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
