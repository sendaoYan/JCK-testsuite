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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
