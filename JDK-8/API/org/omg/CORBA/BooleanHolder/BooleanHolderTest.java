/*
 * @test
 * @summary org.omg.CORBA.BooleanHolder conformance: holder value field.
 * @run main BooleanHolderTest
 */

public class BooleanHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.BooleanHolder h0 = new org.omg.CORBA.BooleanHolder();
        check(h0.value == false, "no-arg default value");
        boolean v = true;
        org.omg.CORBA.BooleanHolder h1 = new org.omg.CORBA.BooleanHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.BooleanHolder.class),
              "BooleanHolder implements org.omg.CORBA.portable.Streamable");
        check(new org.omg.CORBA.BooleanHolder(true).value, "ctor stores true");
        org.omg.CORBA.BooleanHolder whB = new org.omg.CORBA.BooleanHolder(true);
        whB.value = false;
        check(!whB.value, "value field is mutable and reads back");
        check(new org.omg.CORBA.BooleanHolder()._type().kind() == org.omg.CORBA.TCKind.tk_boolean,
              "BooleanHolder._type().kind() == tk_boolean");
        check(new org.omg.CORBA.BooleanHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean)),
              "BooleanHolder._type().equal(get_primitive_tc(tk_boolean))");
        org.omg.CORBA.portable.OutputStream osW9 = orb.create_output_stream();
        new org.omg.CORBA.BooleanHolder(true)._write(osW9);
        org.omg.CORBA.BooleanHolder whRt9 = new org.omg.CORBA.BooleanHolder();
        whRt9._read(osW9.create_input_stream());
        check(whRt9.value, "BooleanHolder CDR wire round-trip value true");
        org.omg.CORBA.portable.OutputStream osW10 = orb.create_output_stream();
        new org.omg.CORBA.BooleanHolder(false)._write(osW10);
        org.omg.CORBA.BooleanHolder whRt10 = new org.omg.CORBA.BooleanHolder();
        whRt10._read(osW10.create_input_stream());
        check(!whRt10.value, "BooleanHolder CDR wire round-trip value false");
        org.omg.CORBA.Any anyB = orb.create_any();
        anyB.insert_boolean(true);
        org.omg.CORBA.BooleanHolder whBA = new org.omg.CORBA.BooleanHolder();
        whBA._read(anyB.create_input_stream());
        check(whBA.value, "Any channel: insert_boolean -> holder._read == true");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
