/*
 * @test
 * @summary org.omg.CosNaming.NameComponent conformance: value type fields and constructors.
 * @run main NameComponentTest
 */

public class NameComponentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NameComponent s0 = new org.omg.CosNaming.NameComponent();
        check(s0.id == null, "no-arg id default");
        check(s0.kind == null, "no-arg kind default");
        org.omg.CosNaming.NameComponent s1 = new org.omg.CosNaming.NameComponent("s", "s");
        check(s1.id == "s", "ctor id stored");
        check(s1.kind == "s", "ctor kind stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
