/*
 * @test
 * @summary org.omg.DynamicAny.NameValuePair conformance: value type fields and constructors.
 * @run main NameValuePairTest
 */

public class NameValuePairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.DynamicAny.NameValuePair s0 = new org.omg.DynamicAny.NameValuePair();
        check(s0.id == null, "no-arg id default");
        check(s0.value == null, "no-arg value default");
        org.omg.DynamicAny.NameValuePair s1 = new org.omg.DynamicAny.NameValuePair("s", null);
        check(s1.id == "s", "ctor id stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
