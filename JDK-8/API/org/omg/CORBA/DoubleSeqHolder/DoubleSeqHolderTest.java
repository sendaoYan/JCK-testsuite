/*
 * @test
 * @summary org.omg.CORBA.DoubleSeqHolder conformance: holder value field.
 * @run main DoubleSeqHolderTest
 */

public class DoubleSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.DoubleSeqHolder h0 = new org.omg.CORBA.DoubleSeqHolder();
        check(h0.value == null, "no-arg default value");
        double[] v = null;
        org.omg.CORBA.DoubleSeqHolder h1 = new org.omg.CORBA.DoubleSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.DoubleSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "DoubleSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.DoubleSeqHolder().value == null, "DoubleSeqHolder default value is null (array type)");
        double[] arrW = new double[]{-2.5};
        check(new org.omg.CORBA.DoubleSeqHolder(arrW).value == arrW, "DoubleSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.DoubleSeqHolder()._type().equal(org.omg.CORBA.DoubleSeqHelper.type()),
              "DoubleSeqHolder._type().equal(DoubleSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.DoubleSeqHolder(new double[]{-2.5, 4.75})._write(osH);
        org.omg.CORBA.DoubleSeqHolder whH = new org.omg.CORBA.DoubleSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new double[]{-2.5, 4.75}), "DoubleSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
