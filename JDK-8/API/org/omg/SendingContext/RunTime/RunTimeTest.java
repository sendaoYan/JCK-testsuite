/*
 * @test
 * @summary org.omg.SendingContext.RunTime conformance: interface shape.
 * @run main RunTimeTest
 */

public class RunTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.SendingContext.RunTime");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.SendingContext.RunTimeOperations.class.isAssignableFrom(c), "extends org.omg.SendingContext.RunTimeOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
