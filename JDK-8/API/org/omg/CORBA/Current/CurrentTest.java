/*
 * @test
 * @summary org.omg.CORBA.Current conformance: interface shape.
 * @run main CurrentTest
 */

public class CurrentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.Current");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.CurrentOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.CurrentOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
