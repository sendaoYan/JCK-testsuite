/*
 * @test
 * @summary org.omg.CORBA.FloatHolder conformance: holder value field.
 * @run main FloatHolderTest
 */

public class FloatHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.FloatHolder h0 = new org.omg.CORBA.FloatHolder();
        check(h0.value == 0.0f, "no-arg default value");
        float v = 7.0f;
        org.omg.CORBA.FloatHolder h1 = new org.omg.CORBA.FloatHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
