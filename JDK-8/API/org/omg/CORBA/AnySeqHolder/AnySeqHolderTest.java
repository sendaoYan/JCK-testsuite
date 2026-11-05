/*
 * @test
 * @summary org.omg.CORBA.AnySeqHolder conformance: holder value field.
 * @run main AnySeqHolderTest
 */

public class AnySeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.AnySeqHolder h0 = new org.omg.CORBA.AnySeqHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Any[] v = null;
        org.omg.CORBA.AnySeqHolder h1 = new org.omg.CORBA.AnySeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.AnySeqHolder().value == null, "default value is null");
        check(new org.omg.CORBA.AnySeqHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "AnySeqHolder._type().kind() == tk_alias");
        check(new org.omg.CORBA.AnySeqHolder()._type().equal(org.omg.CORBA.AnySeqHelper.type()),
              "AnySeqHolder._type().equal(AnySeqHelper.type())");
        org.omg.CORBA.Any anySh = orb.create_any();
        anySh.insert_short((short) 9);
        org.omg.CORBA.portable.OutputStream osSh = orb.create_output_stream();
        new org.omg.CORBA.AnySeqHolder(new org.omg.CORBA.Any[]{anySh})._write(osSh);
        org.omg.CORBA.AnySeqHolder whSh = new org.omg.CORBA.AnySeqHolder();
        whSh._read(osSh.create_input_stream());
        check(whSh.value.length == 1 && whSh.value[0].extract_short() == (short) 9,
              "Any[] CDR wire round-trip with typed element");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
