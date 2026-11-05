/*
 * @test
 * @summary org.omg.CORBA.ULongSeqHolder conformance: holder value field.
 * @run main ULongSeqHolderTest
 */

public class ULongSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ULongSeqHolder h0 = new org.omg.CORBA.ULongSeqHolder();
        check(h0.value == null, "no-arg default value");
        int[] v = null;
        org.omg.CORBA.ULongSeqHolder h1 = new org.omg.CORBA.ULongSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.ULongSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "ULongSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.ULongSeqHolder().value == null, "ULongSeqHolder default value is null (array type)");
        int[] arrW = new int[]{2100000000};
        check(new org.omg.CORBA.ULongSeqHolder(arrW).value == arrW, "ULongSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.ULongSeqHolder()._type().equal(org.omg.CORBA.ULongSeqHelper.type()),
              "ULongSeqHolder._type().equal(ULongSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.ULongSeqHolder(new int[]{2100000000})._write(osH);
        org.omg.CORBA.ULongSeqHolder whH = new org.omg.CORBA.ULongSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new int[]{2100000000}), "ULongSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
