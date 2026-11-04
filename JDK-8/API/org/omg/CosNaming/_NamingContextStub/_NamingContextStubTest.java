/*
 * @test
 * @summary org.omg.CosNaming._NamingContextStub conformance: stub/tie class shape.
 * @run main _NamingContextStubTest
 */

public class _NamingContextStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming._NamingContextStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.CosNaming.NamingContext.class.isAssignableFrom(c), "implements org.omg.CosNaming.NamingContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
