/*
 * @test
 * @summary org.omg.CORBA.WCharSeqHolder conformance: holder value field.
 * @run main WCharSeqHolderTest
 */

public class WCharSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.WCharSeqHolder h0 = new org.omg.CORBA.WCharSeqHolder();
        check(h0.value == null, "no-arg default value");
        char[] v = null;
        org.omg.CORBA.WCharSeqHolder h1 = new org.omg.CORBA.WCharSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.WCharSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "WCharSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.WCharSeqHolder().value == null, "WCharSeqHolder default value is null (array type)");
        char[] arrW = new char[]{'\u4f60'};
        check(new org.omg.CORBA.WCharSeqHolder(arrW).value == arrW, "WCharSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.WCharSeqHolder()._type().equal(org.omg.CORBA.WCharSeqHelper.type()),
              "WCharSeqHolder._type().equal(WCharSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.WCharSeqHolder(new char[]{'\u4f60'})._write(osH);
        org.omg.CORBA.WCharSeqHolder whH = new org.omg.CORBA.WCharSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new char[]{'\u4f60'}), "WCharSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
