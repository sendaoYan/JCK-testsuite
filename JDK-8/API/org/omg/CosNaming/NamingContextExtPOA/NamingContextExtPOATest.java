/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPOA conformance: abstract class shape.
 * @run main NamingContextExtPOATest
 */

public class NamingContextExtPOATest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextExtPOA");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.PortableServer.Servant.class.isAssignableFrom(c), "extends org.omg.PortableServer.Servant");
        check(org.omg.CosNaming.NamingContextExtOperations.class.isAssignableFrom(c), "implements org.omg.CosNaming.NamingContextExtOperations");
        check(org.omg.CORBA.portable.InvokeHandler.class.isAssignableFrom(c), "implements org.omg.CORBA.portable.InvokeHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
