/*
 * @test
 * @summary org.omg.CORBA.ULongLongSeqHolder conformance: holder value field.
 * @run main ULongLongSeqHolderTest
 */

public class ULongLongSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ULongLongSeqHolder h0 = new org.omg.CORBA.ULongLongSeqHolder();
        check(h0.value == null, "no-arg default value");
        long[] v = null;
        org.omg.CORBA.ULongLongSeqHolder h1 = new org.omg.CORBA.ULongLongSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.ULongLongSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "ULongLongSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.ULongLongSeqHolder().value == null, "ULongLongSeqHolder default value is null (array type)");
        long[] arrW = new long[]{123456789012345L};
        check(new org.omg.CORBA.ULongLongSeqHolder(arrW).value == arrW, "ULongLongSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.ULongLongSeqHolder()._type().equal(org.omg.CORBA.ULongLongSeqHelper.type()),
              "ULongLongSeqHolder._type().equal(ULongLongSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.ULongLongSeqHolder(new long[]{Long.MAX_VALUE})._write(osH);
        org.omg.CORBA.ULongLongSeqHolder whH = new org.omg.CORBA.ULongLongSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new long[]{Long.MAX_VALUE}), "ULongLongSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
