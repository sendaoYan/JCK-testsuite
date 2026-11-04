/*
 * @test
 * @summary org.omg.CORBA.UnknownUserExceptionHolder conformance: holder value field.
 * @run main UnknownUserExceptionHolderTest
 */

public class UnknownUserExceptionHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.UnknownUserExceptionHolder h0 = new org.omg.CORBA.UnknownUserExceptionHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.UnknownUserException v = null;
        org.omg.CORBA.UnknownUserExceptionHolder h1 = new org.omg.CORBA.UnknownUserExceptionHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
