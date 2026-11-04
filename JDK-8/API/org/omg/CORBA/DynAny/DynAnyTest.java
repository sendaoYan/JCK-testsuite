/*
 * @test
 * @summary org.omg.CORBA.DynAny conformance: interface shape.
 * @run main DynAnyTest
 */

public class DynAnyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DynAny");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
