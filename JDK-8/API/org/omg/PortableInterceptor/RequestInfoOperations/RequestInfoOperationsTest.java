/*
 * @test
 * @summary org.omg.PortableInterceptor.RequestInfoOperations conformance: interface shape.
 * @run main RequestInfoOperationsTest
 */

public class RequestInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.RequestInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
