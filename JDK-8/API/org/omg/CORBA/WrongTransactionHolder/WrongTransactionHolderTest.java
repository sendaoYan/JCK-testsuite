/*
 * @test
 * @summary org.omg.CORBA.WrongTransactionHolder conformance: holder value field.
 * @run main WrongTransactionHolderTest
 */

public class WrongTransactionHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.WrongTransactionHolder h0 = new org.omg.CORBA.WrongTransactionHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.WrongTransaction v = null;
        org.omg.CORBA.WrongTransactionHolder h1 = new org.omg.CORBA.WrongTransactionHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.WrongTransactionHolder().value == null,
              "default value is null (IDL exception Holder)");
        org.omg.CORBA.WrongTransaction wt = new org.omg.CORBA.WrongTransaction("w");
        check(new org.omg.CORBA.WrongTransactionHolder(wt).value == wt, "ctor stores same reference");
        check(new org.omg.CORBA.WrongTransactionHolder()._type().kind() == org.omg.CORBA.TCKind.tk_except,
              "WrongTransactionHolder._type().kind() == tk_except");
        check(new org.omg.CORBA.WrongTransactionHolder()._type().equal(org.omg.CORBA.WrongTransactionHelper.type()),
              "WrongTransactionHolder._type().equal(WrongTransactionHelper.type())");
        org.omg.CORBA.portable.OutputStream osWt = orb.create_output_stream();
        new org.omg.CORBA.WrongTransactionHolder(wt)._write(osWt);
        org.omg.CORBA.WrongTransactionHolder whWt = new org.omg.CORBA.WrongTransactionHolder();
        whWt._read(osWt.create_input_stream());
        check("IDL:omg.org/CORBA/WrongTransaction:1.0".equals(whWt.value.getMessage()),
              "wire read-back carries repository id as message");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.WrongTransactionHolder.class),
              "WrongTransactionHolder implements org.omg.CORBA.portable.Streamable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
