/*
 * @test
 * @summary org.omg.CORBA.UShortSeqHolder conformance: holder value field.
 * @run main UShortSeqHolderTest
 */

public class UShortSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.UShortSeqHolder h0 = new org.omg.CORBA.UShortSeqHolder();
        check(h0.value == null, "no-arg default value");
        short[] v = null;
        org.omg.CORBA.UShortSeqHolder h1 = new org.omg.CORBA.UShortSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.UShortSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "UShortSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.UShortSeqHolder().value == null, "UShortSeqHolder default value is null (array type)");
        short[] arrW = new short[]{(short) 4000};
        check(new org.omg.CORBA.UShortSeqHolder(arrW).value == arrW, "UShortSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.UShortSeqHolder()._type().equal(org.omg.CORBA.UShortSeqHelper.type()),
              "UShortSeqHolder._type().equal(UShortSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.UShortSeqHolder(new short[]{(short) -300, (short) 300})._write(osH);
        org.omg.CORBA.UShortSeqHolder whH = new org.omg.CORBA.UShortSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new short[]{(short) -300, (short) 300}), "UShortSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
