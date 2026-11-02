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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
