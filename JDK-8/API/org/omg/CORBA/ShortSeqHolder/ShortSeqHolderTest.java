/*
 * @test
 * @summary org.omg.CORBA.ShortSeqHolder conformance: holder value field.
 * @run main ShortSeqHolderTest
 */

public class ShortSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ShortSeqHolder h0 = new org.omg.CORBA.ShortSeqHolder();
        check(h0.value == null, "no-arg default value");
        short[] v = null;
        org.omg.CORBA.ShortSeqHolder h1 = new org.omg.CORBA.ShortSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.ShortSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "ShortSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.ShortSeqHolder().value == null, "ShortSeqHolder default value is null (array type)");
        short[] arrW = new short[]{(short) 7};
        check(new org.omg.CORBA.ShortSeqHolder(arrW).value == arrW, "ShortSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.ShortSeqHolder()._type().equal(org.omg.CORBA.ShortSeqHelper.type()),
              "ShortSeqHolder._type().equal(ShortSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.ShortSeqHolder(new short[]{(short) -300, (short) 300})._write(osH);
        org.omg.CORBA.ShortSeqHolder whH = new org.omg.CORBA.ShortSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new short[]{(short) -300, (short) 300}), "ShortSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
