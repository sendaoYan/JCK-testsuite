/*
 * @test
 * @summary org.omg.SendingContext.RunTimeOperations conformance: interface shape.
 * @run main RunTimeOperationsTest
 */

public class RunTimeOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.SendingContext.RunTimeOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): empty operations interface ----
        check(c.getDeclaredMethods().length == 0, "RunTimeOperations declares zero methods (SendingContext::RunTime has no operations in this mapping)");
        check(c.getInterfaces().length == 0, "RunTimeOperations extends nothing");
        check(c.getDeclaredFields().length == 0, "RunTimeOperations declares zero fields");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.SendingContext.RunTime.class),
              "conformance interface RunTime still carries CORBA::Object through IDLEntity mapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
