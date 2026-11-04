/*
 * @test
 * @summary org.omg.CORBA.LocalObject conformance: concrete class behavior.
 * @run main LocalObjectTest
 */

public class LocalObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.LocalObject lo = new org.omg.CORBA.LocalObject();
        check(lo instanceof org.omg.CORBA.Object, "LocalObject implements org.omg.CORBA.Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
