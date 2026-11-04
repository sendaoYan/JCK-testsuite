/*
 * @test
 * @summary org.omg.CORBA.BooleanHolder conformance: holder value field.
 * @run main BooleanHolderTest
 */

public class BooleanHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.BooleanHolder h0 = new org.omg.CORBA.BooleanHolder();
        check(h0.value == false, "no-arg default value");
        boolean v = true;
        org.omg.CORBA.BooleanHolder h1 = new org.omg.CORBA.BooleanHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
