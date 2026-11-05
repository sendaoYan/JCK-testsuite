/*
 * @test
 * @summary org.omg.CORBA.PolicyListHolder conformance: holder value field.
 * @run main PolicyListHolderTest
 */

public class PolicyListHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.PolicyListHolder h0 = new org.omg.CORBA.PolicyListHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Policy[] v = null;
        org.omg.CORBA.PolicyListHolder h1 = new org.omg.CORBA.PolicyListHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.PolicyListHolder().value == null, "default value is null");
        org.omg.CORBA.Policy[] plArr = new org.omg.CORBA.Policy[0];
        check(new org.omg.CORBA.PolicyListHolder(plArr).value == plArr, "ctor stores same array reference");
        check(new org.omg.CORBA.PolicyListHolder()._type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "PolicyListHolder._type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osPl0 = orb.create_output_stream();
        new org.omg.CORBA.PolicyListHolder(new org.omg.CORBA.Policy[0])._write(osPl0);
        org.omg.CORBA.PolicyListHolder whPl0 = new org.omg.CORBA.PolicyListHolder();
        whPl0._read(osPl0.create_input_stream());
        check(whPl0.value.length == 0, "empty array wire round-trip keeps length 0");
        org.omg.CORBA.portable.OutputStream osPl1 = orb.create_output_stream();
        new org.omg.CORBA.PolicyListHolder(new org.omg.CORBA.Policy[]{null})._write(osPl1);
        org.omg.CORBA.PolicyListHolder whPl1 = new org.omg.CORBA.PolicyListHolder();
        whPl1._read(osPl1.create_input_stream());
        check(whPl1.value.length == 1 && whPl1.value[0] == null, "nil-element array wire round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
