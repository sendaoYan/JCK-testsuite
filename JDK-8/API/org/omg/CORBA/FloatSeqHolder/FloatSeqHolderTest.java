/*
 * @test
 * @summary org.omg.CORBA.FloatSeqHolder conformance: holder value field.
 * @run main FloatSeqHolderTest
 */

public class FloatSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.FloatSeqHolder h0 = new org.omg.CORBA.FloatSeqHolder();
        check(h0.value == null, "no-arg default value");
        float[] v = null;
        org.omg.CORBA.FloatSeqHolder h1 = new org.omg.CORBA.FloatSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.FloatSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "FloatSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.FloatSeqHolder().value == null, "FloatSeqHolder default value is null (array type)");
        float[] arrW = new float[]{1.25f};
        check(new org.omg.CORBA.FloatSeqHolder(arrW).value == arrW, "FloatSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.FloatSeqHolder()._type().equal(org.omg.CORBA.FloatSeqHelper.type()),
              "FloatSeqHolder._type().equal(FloatSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.FloatSeqHolder(new float[]{1.25f})._write(osH);
        org.omg.CORBA.FloatSeqHolder whH = new org.omg.CORBA.FloatSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new float[]{1.25f}), "FloatSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
