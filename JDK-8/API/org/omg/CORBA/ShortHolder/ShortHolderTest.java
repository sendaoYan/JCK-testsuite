/*
 * @test
 * @summary org.omg.CORBA.ShortHolder conformance: holder value field.
 * @run main ShortHolderTest
 */

public class ShortHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ShortHolder h0 = new org.omg.CORBA.ShortHolder();
        check(h0.value == (short)0, "no-arg default value");
        short v = (short)7;
        org.omg.CORBA.ShortHolder h1 = new org.omg.CORBA.ShortHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.ShortHolder.class),
              "ShortHolder implements org.omg.CORBA.portable.Streamable");
        check(new org.omg.CORBA.ShortHolder(Short.MIN_VALUE).value == Short.MIN_VALUE, "ctor stores Short.MIN_VALUE");
        check(new org.omg.CORBA.ShortHolder(Short.MAX_VALUE).value == Short.MAX_VALUE, "ctor stores Short.MAX_VALUE");
        org.omg.CORBA.ShortHolder whS = new org.omg.CORBA.ShortHolder((short) 1);
        whS.value = (short) -7;
        check(whS.value == (short) -7, "value field is mutable and reads back");
        check(new org.omg.CORBA.ShortHolder()._type().kind() == org.omg.CORBA.TCKind.tk_short,
              "ShortHolder._type().kind() == tk_short");
        check(new org.omg.CORBA.ShortHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_short)),
              "ShortHolder._type().equal(get_primitive_tc(tk_short))");
        org.omg.CORBA.portable.OutputStream osW5 = orb.create_output_stream();
        new org.omg.CORBA.ShortHolder((short) 300)._write(osW5);
        org.omg.CORBA.ShortHolder whRt5 = new org.omg.CORBA.ShortHolder();
        whRt5._read(osW5.create_input_stream());
        check(whRt5.value == (short) 300, "ShortHolder CDR wire round-trip value 300");
        org.omg.CORBA.portable.OutputStream osW6 = orb.create_output_stream();
        new org.omg.CORBA.ShortHolder((short) -300)._write(osW6);
        org.omg.CORBA.ShortHolder whRt6 = new org.omg.CORBA.ShortHolder();
        whRt6._read(osW6.create_input_stream());
        check(whRt6.value == (short) -300, "ShortHolder CDR wire round-trip value -300");
        org.omg.CORBA.Any anyS = orb.create_any();
        anyS.insert_short((short) 77);
        org.omg.CORBA.ShortHolder whSA = new org.omg.CORBA.ShortHolder();
        whSA._read(anyS.create_input_stream());
        check(whSA.value == (short) 77, "Any channel: insert_short -> holder._read == 77");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
