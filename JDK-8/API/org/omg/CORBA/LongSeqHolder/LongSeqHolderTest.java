/*
 * @test
 * @summary org.omg.CORBA.LongSeqHolder conformance: holder value field.
 * @run main LongSeqHolderTest
 */

public class LongSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.LongSeqHolder h0 = new org.omg.CORBA.LongSeqHolder();
        check(h0.value == null, "no-arg default value");
        int[] v = null;
        org.omg.CORBA.LongSeqHolder h1 = new org.omg.CORBA.LongSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.LongSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "LongSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.LongSeqHolder().value == null, "LongSeqHolder default value is null (array type)");
        int[] arrW = new int[]{1, -2, 3};
        check(new org.omg.CORBA.LongSeqHolder(arrW).value == arrW, "LongSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.LongSeqHolder()._type().equal(org.omg.CORBA.LongSeqHelper.type()),
              "LongSeqHolder._type().equal(LongSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.LongSeqHolder(new int[]{1, -2, 3})._write(osH);
        org.omg.CORBA.LongSeqHolder whH = new org.omg.CORBA.LongSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new int[]{1, -2, 3}), "LongSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
