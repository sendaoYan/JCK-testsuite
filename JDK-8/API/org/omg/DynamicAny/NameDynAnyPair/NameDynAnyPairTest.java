/*
 * @test
 * @summary org.omg.DynamicAny.NameDynAnyPair conformance: value type fields and constructors.
 * @run main NameDynAnyPairTest
 */

public class NameDynAnyPairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.DynamicAny.NameDynAnyPair s0 = new org.omg.DynamicAny.NameDynAnyPair();
        check(s0.id == null, "no-arg id default");
        check(s0.value == null, "no-arg value default");
        org.omg.DynamicAny.NameDynAnyPair s1 = new org.omg.DynamicAny.NameDynAnyPair("s", null);
        check(s1.id == "s", "ctor id stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
