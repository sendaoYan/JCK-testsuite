/*
 * @test
 * @summary org.omg.CORBA.CharHolder conformance: holder value field.
 * @run main CharHolderTest
 */

public class CharHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.CharHolder h0 = new org.omg.CORBA.CharHolder();
        check(h0.value == '\0', "no-arg default value");
        char v = 'x';
        org.omg.CORBA.CharHolder h1 = new org.omg.CORBA.CharHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.CharHolder.class),
              "CharHolder implements org.omg.CORBA.portable.Streamable");
        check(new org.omg.CORBA.CharHolder('Z').value == 'Z', "ctor stores 'Z'");
        check(new org.omg.CORBA.CharHolder('\u0000').value == '\u0000', "ctor stores NUL char");
        org.omg.CORBA.CharHolder whC = new org.omg.CORBA.CharHolder('a');
        whC.value = 'b';
        check(whC.value == 'b', "value field is mutable and reads back");
        check(new org.omg.CORBA.CharHolder()._type().kind() == org.omg.CORBA.TCKind.tk_char,
              "CharHolder._type().kind() == tk_char");
        check(new org.omg.CORBA.CharHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_char)),
              "CharHolder._type().equal(get_primitive_tc(tk_char))");
        org.omg.CORBA.portable.OutputStream osW12 = orb.create_output_stream();
        new org.omg.CORBA.CharHolder('Z')._write(osW12);
        org.omg.CORBA.CharHolder whRt12 = new org.omg.CORBA.CharHolder();
        whRt12._read(osW12.create_input_stream());
        check(whRt12.value == 'Z', "CharHolder CDR wire round-trip value Z");
        org.omg.CORBA.portable.OutputStream osW13 = orb.create_output_stream();
        new org.omg.CORBA.CharHolder('\n')._write(osW13);
        org.omg.CORBA.CharHolder whRt13 = new org.omg.CORBA.CharHolder();
        whRt13._read(osW13.create_input_stream());
        check(whRt13.value == '\n', "CharHolder CDR wire round-trip newline char");
        org.omg.CORBA.Any anyC = orb.create_any();
        anyC.insert_char('q');
        org.omg.CORBA.CharHolder whCA = new org.omg.CORBA.CharHolder();
        whCA._read(anyC.create_input_stream());
        check(whCA.value == 'q', "Any channel: insert_char -> holder._read == 'q'");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
