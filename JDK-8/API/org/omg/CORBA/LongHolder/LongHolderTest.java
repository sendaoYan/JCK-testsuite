/*
 * @test
 * @summary org.omg.CORBA.LongHolder conformance: holder value field.
 * @run main LongHolderTest
 */

public class LongHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.LongHolder h0 = new org.omg.CORBA.LongHolder();
        check(h0.value == 0L, "no-arg default value");
        long v = 7L;
        org.omg.CORBA.LongHolder h1 = new org.omg.CORBA.LongHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.LongHolder.class),
              "LongHolder implements org.omg.CORBA.portable.Streamable");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.LongHolder.class.getModifiers()),
              "LongHolder class is final");
        check(new org.omg.CORBA.LongHolder(Long.MIN_VALUE).value == Long.MIN_VALUE, "ctor stores Long.MIN_VALUE");
        check(new org.omg.CORBA.LongHolder(Long.MAX_VALUE).value == Long.MAX_VALUE, "ctor stores Long.MAX_VALUE");
        org.omg.CORBA.LongHolder whM = new org.omg.CORBA.LongHolder(7L);
        whM.value = -42L;
        check(whM.value == -42L, "value field is mutable and reads back");
        check(new org.omg.CORBA.LongHolder()._type().kind() == org.omg.CORBA.TCKind.tk_longlong,
              "LongHolder._type().kind() == tk_longlong (IDL long long maps to Java long)");
        check(new org.omg.CORBA.LongHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_longlong)),
              "LongHolder._type().equal(get_primitive_tc(tk_longlong))");
        org.omg.CORBA.portable.OutputStream osW1 = orb.create_output_stream();
        new org.omg.CORBA.LongHolder(9L)._write(osW1);
        org.omg.CORBA.LongHolder whRt1 = new org.omg.CORBA.LongHolder();
        whRt1._read(osW1.create_input_stream());
        check(whRt1.value == 9L, "LongHolder CDR wire round-trip value 9");
        org.omg.CORBA.portable.OutputStream osW2 = orb.create_output_stream();
        new org.omg.CORBA.LongHolder(-123L)._write(osW2);
        org.omg.CORBA.LongHolder whRt2 = new org.omg.CORBA.LongHolder();
        whRt2._read(osW2.create_input_stream());
        check(whRt2.value == -123L, "LongHolder CDR wire round-trip negative value");
        org.omg.CORBA.portable.OutputStream os2h = orb.create_output_stream();
        new org.omg.CORBA.LongHolder(1L)._write(os2h);
        new org.omg.CORBA.LongHolder(2L)._write(os2h);
        org.omg.CORBA.portable.InputStream is2h = os2h.create_input_stream();
        org.omg.CORBA.LongHolder whA = new org.omg.CORBA.LongHolder();
        org.omg.CORBA.LongHolder whB = new org.omg.CORBA.LongHolder();
        whA._read(is2h);
        whB._read(is2h);
        check(whA.value == 1L && whB.value == 2L, "two holders back-to-back in one CDR stream round-trip");
        org.omg.CORBA.Any anyLL = orb.create_any();
        anyLL.insert_longlong(5L);
        org.omg.CORBA.LongHolder whAny = new org.omg.CORBA.LongHolder();
        whAny._read(anyLL.create_input_stream());
        check(whAny.value == 5L, "Any channel: insert_longlong -> holder._read == 5");
        org.omg.CORBA.Any anyLw = orb.create_any();
        anyLw.insert_long(-123);
        org.omg.CORBA.IntHolder ihOk = new org.omg.CORBA.IntHolder();
        ihOk._read(anyLw.create_input_stream());
        check(ihOk.value == -123, "IntHolder(tk_long) reads a tk_long Any stream");
        try {
            new org.omg.CORBA.LongHolder()._read(anyLw.create_input_stream());
            throw new AssertionError("LongHolder(tk_longlong)._read over 4-byte tk_long Any stream should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "LongHolder(tk_longlong)._read over 4-byte tk_long Any stream -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
