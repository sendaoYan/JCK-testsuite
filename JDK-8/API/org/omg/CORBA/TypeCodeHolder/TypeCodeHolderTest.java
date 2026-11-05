/*
 * @test
 * @summary org.omg.CORBA.TypeCodeHolder conformance: holder value field.
 * @run main TypeCodeHolderTest
 */

public class TypeCodeHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.TypeCodeHolder h0 = new org.omg.CORBA.TypeCodeHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.TypeCode v = null;
        org.omg.CORBA.TypeCodeHolder h1 = new org.omg.CORBA.TypeCodeHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.TypeCodeHolder.class),
              "TypeCodeHolder implements org.omg.CORBA.portable.Streamable");
        org.omg.CORBA.TypeCode tcStr = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string);
        check(new org.omg.CORBA.TypeCodeHolder(tcStr).value == tcStr, "ctor stores same TypeCode reference");
        check(new org.omg.CORBA.TypeCodeHolder()._type().kind() == org.omg.CORBA.TCKind.tk_TypeCode,
              "TypeCodeHolder._type().kind() == tk_TypeCode");
        check(new org.omg.CORBA.TypeCodeHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_TypeCode)),
              "TypeCodeHolder._type().equal(get_primitive_tc(tk_TypeCode))");
        org.omg.CORBA.portable.OutputStream osTc = orb.create_output_stream();
        new org.omg.CORBA.TypeCodeHolder(tcStr)._write(osTc);
        org.omg.CORBA.TypeCodeHolder whTc = new org.omg.CORBA.TypeCodeHolder();
        whTc._read(osTc.create_input_stream());
        check(whTc.value.kind() == org.omg.CORBA.TCKind.tk_string,
              "primitive TypeCode CDR wire round-trip kind");
        org.omg.CORBA.TypeCode tcEnum = orb.create_enum_tc("IDL:E:1.0", "E", new String[]{"A", "B"});
        org.omg.CORBA.portable.OutputStream osTe = orb.create_output_stream();
        new org.omg.CORBA.TypeCodeHolder(tcEnum)._write(osTe);
        org.omg.CORBA.TypeCodeHolder whTe = new org.omg.CORBA.TypeCodeHolder();
        whTe._read(osTe.create_input_stream());
        check(whTe.value.kind() == org.omg.CORBA.TCKind.tk_enum,
              "complex (enum) TypeCode CDR wire round-trip kind");
        check(whTe.value.member_count() == 2, "complex TypeCode wire round-trip keeps member_count");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
