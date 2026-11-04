/*
 * @test
 * @summary org.omg.CORBA.IDLTypeOperations conformance: interface shape.
 * @run main IDLTypeOperationsTest
 */

public class IDLTypeOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.IDLTypeOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.IRObjectOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.IRObjectOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
