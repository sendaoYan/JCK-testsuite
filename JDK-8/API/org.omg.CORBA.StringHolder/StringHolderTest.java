/*
 * @test
 * @summary org.omg.CORBA.StringHolder conformance: holder value field.
 * @run main StringHolderTest
 */

public class StringHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.StringHolder h0 = new org.omg.CORBA.StringHolder();
        check(h0.value == null, "no-arg default value");
        java.lang.String v = "s";
        org.omg.CORBA.StringHolder h1 = new org.omg.CORBA.StringHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
