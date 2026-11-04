/*
 * @test
 * @summary org.omg.CORBA.NameValuePair conformance: value type fields and constructors.
 * @run main NameValuePairTest
 */

public class NameValuePairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.NameValuePair s0 = new org.omg.CORBA.NameValuePair();
        check(s0.id == null, "no-arg id default");
        check(s0.value == null, "no-arg value default");
        org.omg.CORBA.NameValuePair s1 = new org.omg.CORBA.NameValuePair("s", null);
        check(s1.id == "s", "ctor id stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
