/*
 * @test
 * @summary org.omg.CORBA.CurrentHolder conformance: holder value field.
 * @run main CurrentHolderTest
 */

public class CurrentHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.CurrentHolder h0 = new org.omg.CORBA.CurrentHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Current v = null;
        org.omg.CORBA.CurrentHolder h1 = new org.omg.CORBA.CurrentHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
