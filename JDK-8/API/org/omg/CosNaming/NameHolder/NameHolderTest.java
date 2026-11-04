/*
 * @test
 * @summary org.omg.CosNaming.NameHolder conformance: holder value field.
 * @run main NameHolderTest
 */

public class NameHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NameHolder h0 = new org.omg.CosNaming.NameHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NameComponent[] v = null;
        org.omg.CosNaming.NameHolder h1 = new org.omg.CosNaming.NameHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
