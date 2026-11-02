/*
 * @test
 * @summary org.omg.IOP.MultipleComponentProfileHolder conformance: holder value field.
 * @run main MultipleComponentProfileHolderTest
 */

public class MultipleComponentProfileHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.MultipleComponentProfileHolder h0 = new org.omg.IOP.MultipleComponentProfileHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.IOP.TaggedComponent[] v = null;
        org.omg.IOP.MultipleComponentProfileHolder h1 = new org.omg.IOP.MultipleComponentProfileHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
