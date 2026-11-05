/*
 * @test
 * @summary org.omg.CORBA.OctetSeqHolder conformance: holder value field.
 * @run main OctetSeqHolderTest
 */

public class OctetSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.OctetSeqHolder h0 = new org.omg.CORBA.OctetSeqHolder();
        check(h0.value == null, "no-arg default value");
        byte[] v = null;
        org.omg.CORBA.OctetSeqHolder h1 = new org.omg.CORBA.OctetSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        check(new org.omg.CORBA.OctetSeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "OctetSeqHolder._type().kind() == tk_alias (IDL typedef of a sequence)");
        check(new org.omg.CORBA.OctetSeqHolder().value == null, "OctetSeqHolder default value is null (array type)");
        byte[] arrW = new byte[]{9, 8, 7};
        check(new org.omg.CORBA.OctetSeqHolder(arrW).value == arrW, "OctetSeqHolder(array) stores same array reference");
        check(new org.omg.CORBA.OctetSeqHolder()._type().equal(org.omg.CORBA.OctetSeqHelper.type()),
              "OctetSeqHolder._type().equal(OctetSeqHelper.type())");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        new org.omg.CORBA.OctetSeqHolder(new byte[]{9, 8, 7})._write(osH);
        org.omg.CORBA.OctetSeqHolder whH = new org.omg.CORBA.OctetSeqHolder();
        whH._read(osH.create_input_stream());
        check(java.util.Arrays.equals(whH.value, new byte[]{9, 8, 7}), "OctetSeqHolder CDR wire round-trip element-wise");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
