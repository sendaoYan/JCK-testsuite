/*
 * @test
 * @summary org.omg.CORBA.DynamicImplementation conformance: concrete class behavior.
 * @run main DynamicImplementationTest
 */

public class DynamicImplementationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.DynamicImplementation di = new org.omg.CORBA.DynamicImplementation();
        check(di instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(di instanceof org.omg.CORBA.Object, "implements org.omg.CORBA.Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
