/*
 * @test
 * @summary org.omg.CosNaming._NamingContextImplBase conformance: abstract class shape.
 * @run main _NamingContextImplBaseTest
 */

public class _NamingContextImplBaseTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming._NamingContextImplBase");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.DynamicImplementation.class.isAssignableFrom(c), "extends org.omg.CORBA.DynamicImplementation");
        check(org.omg.CosNaming.NamingContext.class.isAssignableFrom(c), "implements org.omg.CosNaming.NamingContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
