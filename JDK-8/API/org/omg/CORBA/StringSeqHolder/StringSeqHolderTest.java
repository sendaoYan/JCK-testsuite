/*
 * @test
 * @summary org.omg.CORBA.StringSeqHolder conformance: holder value field.
 * @run main StringSeqHolderTest
 */

public class StringSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.StringSeqHolder h0 = new org.omg.CORBA.StringSeqHolder();
        check(h0.value == null, "no-arg default value");
        java.lang.String[] v = null;
        org.omg.CORBA.StringSeqHolder h1 = new org.omg.CORBA.StringSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.StringSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "StringSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.StringSeqHolder().value == null, "StringSeqHolder default value is null (array type)");
        String[] arrW = new String[]{"a", ""};
        check(new org.omg.CORBA.StringSeqHolder(arrW).value == arrW, "StringSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.StringSeqHolder()._type().equal(org.omg.CORBA.StringSeqHelper.type()),
              "StringSeqHolder._type().equal(StringSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.StringSeqHolder(new String[]{"a", "", "c"})._write(osH);
        org.omg.CORBA.StringSeqHolder whH = new org.omg.CORBA.StringSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new String[]{"a", "", "c"}), "StringSeqHolder CDR wire round-trip element-wise");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.StringSeqHolder.class),
              "StringSeqHolder implements org.omg.CORBA.portable.Streamable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
