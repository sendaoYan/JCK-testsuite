/*
 * @test
 * @summary org.omg.CORBA._IDLTypeStub conformance: stub/tie class shape.
 * @run main _IDLTypeStubTest
 */

public class _IDLTypeStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA._IDLTypeStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.CORBA.IDLType.class.isAssignableFrom(c), "implements org.omg.CORBA.IDLType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
