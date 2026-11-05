/*
 * @test
 * @summary org.omg.CORBA.WStringSeqHolder conformance: holder value field.
 * @run main WStringSeqHolderTest
 */

public class WStringSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.WStringSeqHolder h0 = new org.omg.CORBA.WStringSeqHolder();
        check(h0.value == null, "no-arg default value");
        java.lang.String[] v = null;
        org.omg.CORBA.WStringSeqHolder h1 = new org.omg.CORBA.WStringSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.WStringSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "WStringSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.WStringSeqHolder().value == null, "WStringSeqHolder default value is null (array type)");
        String[] arrW = new String[]{"\u4f60\u597d"};
        check(new org.omg.CORBA.WStringSeqHolder(arrW).value == arrW, "WStringSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.WStringSeqHolder()._type().equal(org.omg.CORBA.WStringSeqHelper.type()),
              "WStringSeqHolder._type().equal(WStringSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.WStringSeqHolder(new String[]{"\u4f60\u597d"})._write(osH);
        org.omg.CORBA.WStringSeqHolder whH = new org.omg.CORBA.WStringSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new String[]{"\u4f60\u597d"}), "WStringSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
