/*
 * @test
 * @summary org.omg.CORBA._PolicyStub conformance: stub/tie class shape.
 * @run main _PolicyStubTest
 */

public class _PolicyStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA._PolicyStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.CORBA.Policy.class.isAssignableFrom(c), "implements org.omg.CORBA.Policy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
