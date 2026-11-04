/*
 * @test
 * @summary org.omg.CosNaming.Binding conformance: value type fields and constructors.
 * @run main BindingTest
 */

public class BindingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.Binding s0 = new org.omg.CosNaming.Binding();
        check(s0.binding_name == null, "no-arg binding_name default");
        check(s0.binding_type == null, "no-arg binding_type default");
        org.omg.CosNaming.Binding s1 = new org.omg.CosNaming.Binding(null, null);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
