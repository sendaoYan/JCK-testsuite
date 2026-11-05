/*
 * @test
 * @summary org.omg.CORBA.BooleanSeqHolder conformance: holder value field.
 * @run main BooleanSeqHolderTest
 */

public class BooleanSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.BooleanSeqHolder h0 = new org.omg.CORBA.BooleanSeqHolder();
        check(h0.value == null, "no-arg default value");
        boolean[] v = null;
        org.omg.CORBA.BooleanSeqHolder h1 = new org.omg.CORBA.BooleanSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.BooleanSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "BooleanSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.BooleanSeqHolder().value == null, "BooleanSeqHolder default value is null (array type)");
        boolean[] arrW = new boolean[]{true};
        check(new org.omg.CORBA.BooleanSeqHolder(arrW).value == arrW, "BooleanSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.BooleanSeqHolder()._type().equal(org.omg.CORBA.BooleanSeqHelper.type()),
              "BooleanSeqHolder._type().equal(BooleanSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.BooleanSeqHolder(new boolean[]{true, false})._write(osH);
        org.omg.CORBA.BooleanSeqHolder whH = new org.omg.CORBA.BooleanSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new boolean[]{true, false}), "BooleanSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
