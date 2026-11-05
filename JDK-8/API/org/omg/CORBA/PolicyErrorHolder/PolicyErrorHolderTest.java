/*
 * @test
 * @summary org.omg.CORBA.PolicyErrorHolder conformance: holder value field.
 * @run main PolicyErrorHolderTest
 */

public class PolicyErrorHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.PolicyErrorHolder h0 = new org.omg.CORBA.PolicyErrorHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.PolicyError v = null;
        org.omg.CORBA.PolicyErrorHolder h1 = new org.omg.CORBA.PolicyErrorHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.PolicyErrorHolder().value == null, "default value is null");
        org.omg.CORBA.PolicyError pe = new org.omg.CORBA.PolicyError((short) 3);
        check(new org.omg.CORBA.PolicyErrorHolder(pe).value == pe, "ctor stores same reference");
        check(new org.omg.CORBA.PolicyErrorHolder()._type().kind() == org.omg.CORBA.TCKind.tk_except,
              "PolicyErrorHolder._type().kind() == tk_except");
        org.omg.CORBA.portable.OutputStream osPe = orb.create_output_stream();
        new org.omg.CORBA.PolicyErrorHolder(pe)._write(osPe);
        org.omg.CORBA.PolicyErrorHolder whPe = new org.omg.CORBA.PolicyErrorHolder();
        whPe._read(osPe.create_input_stream());
        check(whPe.value.reason == 3, "CDR wire round-trip keeps reason");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
