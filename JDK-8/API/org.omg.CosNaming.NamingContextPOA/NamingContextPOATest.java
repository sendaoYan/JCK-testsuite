/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPOA conformance: abstract class shape.
 * @run main NamingContextPOATest
 */

public class NamingContextPOATest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextPOA");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.PortableServer.Servant.class.isAssignableFrom(c), "extends org.omg.PortableServer.Servant");
        check(org.omg.CosNaming.NamingContextOperations.class.isAssignableFrom(c), "implements org.omg.CosNaming.NamingContextOperations");
        check(org.omg.CORBA.portable.InvokeHandler.class.isAssignableFrom(c), "implements org.omg.CORBA.portable.InvokeHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
