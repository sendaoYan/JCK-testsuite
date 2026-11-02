/*
 * @test
 * @summary org.omg.CORBA.IntHolder conformance: holder value field.
 * @run main IntHolderTest
 */

public class IntHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.IntHolder h0 = new org.omg.CORBA.IntHolder();
        check(h0.value == 0, "no-arg default value");
        int v = 7;
        org.omg.CORBA.IntHolder h1 = new org.omg.CORBA.IntHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
