/*
 * @test
 * @summary org.omg.CORBA.ObjectHolder conformance: holder value field.
 * @run main ObjectHolderTest
 */

public class ObjectHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ObjectHolder h0 = new org.omg.CORBA.ObjectHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Object v = null;
        org.omg.CORBA.ObjectHolder h1 = new org.omg.CORBA.ObjectHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
