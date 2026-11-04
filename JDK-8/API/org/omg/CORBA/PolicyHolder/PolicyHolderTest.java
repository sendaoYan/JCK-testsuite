/*
 * @test
 * @summary org.omg.CORBA.PolicyHolder conformance: holder value field.
 * @run main PolicyHolderTest
 */

public class PolicyHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.PolicyHolder h0 = new org.omg.CORBA.PolicyHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Policy v = null;
        org.omg.CORBA.PolicyHolder h1 = new org.omg.CORBA.PolicyHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
