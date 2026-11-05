/*
 * @test
 * @summary org.omg.CORBA.CharSeqHolder conformance: holder value field.
 * @run main CharSeqHolderTest
 */

public class CharSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.CharSeqHolder h0 = new org.omg.CORBA.CharSeqHolder();
        check(h0.value == null, "no-arg default value");
        char[] v = null;
        org.omg.CORBA.CharSeqHolder h1 = new org.omg.CORBA.CharSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.CharSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "CharSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.CharSeqHolder().value == null, "CharSeqHolder default value is null (array type)");
        char[] arrW = new char[]{'x'};
        check(new org.omg.CORBA.CharSeqHolder(arrW).value == arrW, "CharSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.CharSeqHolder()._type().equal(org.omg.CORBA.CharSeqHelper.type()),
              "CharSeqHolder._type().equal(CharSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.CharSeqHolder(new char[]{'x', 'y'})._write(osH);
        org.omg.CORBA.CharSeqHolder whH = new org.omg.CORBA.CharSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new char[]{'x', 'y'}), "CharSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
