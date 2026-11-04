/*
 * @test
 * @summary org.omg.CORBA.CharHolder conformance: holder value field.
 * @run main CharHolderTest
 */

public class CharHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.CharHolder h0 = new org.omg.CORBA.CharHolder();
        check(h0.value == '\0', "no-arg default value");
        char v = 'x';
        org.omg.CORBA.CharHolder h1 = new org.omg.CORBA.CharHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
