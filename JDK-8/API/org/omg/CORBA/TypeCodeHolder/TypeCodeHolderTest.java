/*
 * @test
 * @summary org.omg.CORBA.TypeCodeHolder conformance: holder value field.
 * @run main TypeCodeHolderTest
 */

public class TypeCodeHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.TypeCodeHolder h0 = new org.omg.CORBA.TypeCodeHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.TypeCode v = null;
        org.omg.CORBA.TypeCodeHolder h1 = new org.omg.CORBA.TypeCodeHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
