/*
 * @test
 * @summary org.omg.CORBA.ByteHolder conformance: holder value field.
 * @run main ByteHolderTest
 */

public class ByteHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ByteHolder h0 = new org.omg.CORBA.ByteHolder();
        check(h0.value == (byte)0, "no-arg default value");
        byte v = (byte)7;
        org.omg.CORBA.ByteHolder h1 = new org.omg.CORBA.ByteHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.ByteHolder.class),
              "ByteHolder implements org.omg.CORBA.portable.Streamable");
        check(new org.omg.CORBA.ByteHolder((byte) -3).value == (byte) -3, "ctor stores negative");
        check(new org.omg.CORBA.ByteHolder((byte) 127).value == (byte) 127, "ctor stores max octet");
        org.omg.CORBA.ByteHolder whBy = new org.omg.CORBA.ByteHolder((byte) 1);
        whBy.value = (byte) 9;
        check(whBy.value == (byte) 9, "value field is mutable and reads back");
        check(new org.omg.CORBA.ByteHolder()._type().kind() == org.omg.CORBA.TCKind.tk_octet,
              "ByteHolder._type().kind() == tk_octet (Java byte maps to IDL octet)");
        check(new org.omg.CORBA.ByteHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_octet)),
              "ByteHolder._type().equal(get_primitive_tc(tk_octet))");
        org.omg.CORBA.portable.OutputStream osW11 = orb.create_output_stream();
        new org.omg.CORBA.ByteHolder((byte) -3)._write(osW11);
        org.omg.CORBA.ByteHolder whRt11 = new org.omg.CORBA.ByteHolder();
        whRt11._read(osW11.create_input_stream());
        check(whRt11.value == (byte) -3, "ByteHolder CDR wire round-trip value -3");
        org.omg.CORBA.Any anyBy = orb.create_any();
        anyBy.insert_octet((byte) 9);
        org.omg.CORBA.ByteHolder whByA = new org.omg.CORBA.ByteHolder();
        whByA._read(anyBy.create_input_stream());
        check(whByA.value == (byte) 9, "Any channel: insert_octet -> holder._read == 9");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
