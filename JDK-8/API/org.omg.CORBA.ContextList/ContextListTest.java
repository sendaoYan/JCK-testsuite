/*
 * @test
 * @summary org.omg.CORBA.ContextList conformance: abstract class shape.
 * @run main ContextListTest
 */

public class ContextListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.ContextList");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
