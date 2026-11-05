/*
 * @test
 * @summary org.omg.CORBA.IntHolder conformance: holder value field.
 * @run main IntHolderTest
 */

public class IntHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.IntHolder h0 = new org.omg.CORBA.IntHolder();
        check(h0.value == 0, "no-arg default value");
        int v = 7;
        org.omg.CORBA.IntHolder h1 = new org.omg.CORBA.IntHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.IntHolder.class),
              "IntHolder implements org.omg.CORBA.portable.Streamable");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.IntHolder.class.getModifiers()),
              "IntHolder class is final");
        check(new org.omg.CORBA.IntHolder(Integer.MIN_VALUE).value == Integer.MIN_VALUE, "ctor stores Integer.MIN_VALUE");
        check(new org.omg.CORBA.IntHolder(Integer.MAX_VALUE).value == Integer.MAX_VALUE, "ctor stores Integer.MAX_VALUE");
        org.omg.CORBA.IntHolder whI = new org.omg.CORBA.IntHolder(1);
        whI.value = 55;
        check(whI.value == 55, "value field is mutable and reads back");
        check(new org.omg.CORBA.IntHolder()._type().kind() == org.omg.CORBA.TCKind.tk_long,
              "IntHolder._type().kind() == tk_long (IDL long maps to Java int)");
        check(new org.omg.CORBA.IntHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long)),
              "IntHolder._type().equal(get_primitive_tc(tk_long))");
        org.omg.CORBA.portable.OutputStream osW3 = orb.create_output_stream();
        new org.omg.CORBA.IntHolder(-5)._write(osW3);
        org.omg.CORBA.IntHolder whRt3 = new org.omg.CORBA.IntHolder();
        whRt3._read(osW3.create_input_stream());
        check(whRt3.value == -5, "IntHolder CDR wire round-trip value -5");
        org.omg.CORBA.portable.OutputStream osW4 = orb.create_output_stream();
        new org.omg.CORBA.IntHolder(900)._write(osW4);
        org.omg.CORBA.IntHolder whRt4 = new org.omg.CORBA.IntHolder();
        whRt4._read(osW4.create_input_stream());
        check(whRt4.value == 900, "IntHolder CDR wire round-trip value 900");
        org.omg.CORBA.Any anyI = orb.create_any();
        anyI.insert_long(-123);
        org.omg.CORBA.IntHolder whIA = new org.omg.CORBA.IntHolder();
        whIA._read(anyI.create_input_stream());
        check(whIA.value == -123, "Any channel: insert_long -> holder._read == -123");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
