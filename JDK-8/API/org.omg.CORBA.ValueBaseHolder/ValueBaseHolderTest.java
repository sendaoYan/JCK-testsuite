/*
 * @test
 * @summary org.omg.CORBA.ValueBaseHolder conformance: holder value field.
 * @run main ValueBaseHolderTest
 */

public class ValueBaseHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ValueBaseHolder h0 = new org.omg.CORBA.ValueBaseHolder();
        check(h0.value == null, "no-arg default value");
        java.io.Serializable v = null;
        org.omg.CORBA.ValueBaseHolder h1 = new org.omg.CORBA.ValueBaseHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
