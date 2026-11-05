/*
 * @test
 * @summary org.omg.CORBA.LongLongSeqHolder conformance: holder value field.
 * @run main LongLongSeqHolderTest
 */

public class LongLongSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.LongLongSeqHolder h0 = new org.omg.CORBA.LongLongSeqHolder();
        check(h0.value == null, "no-arg default value");
        long[] v = null;
        org.omg.CORBA.LongLongSeqHolder h1 = new org.omg.CORBA.LongLongSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.LongLongSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "LongLongSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.LongLongSeqHolder().value == null, "LongLongSeqHolder default value is null (array type)");
        long[] arrW = new long[]{-1L};
        check(new org.omg.CORBA.LongLongSeqHolder(arrW).value == arrW, "LongLongSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.LongLongSeqHolder()._type().equal(org.omg.CORBA.LongLongSeqHelper.type()),
              "LongLongSeqHolder._type().equal(LongLongSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.LongLongSeqHolder(new long[]{Long.MIN_VALUE, Long.MAX_VALUE})._write(osH);
        org.omg.CORBA.LongLongSeqHolder whH = new org.omg.CORBA.LongLongSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new long[]{Long.MIN_VALUE, Long.MAX_VALUE}), "LongLongSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
