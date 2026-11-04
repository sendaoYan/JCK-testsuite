/*
 * @test
 * @summary org.omg.DynamicAny._DynAnyStub conformance: stub/tie class shape.
 * @run main _DynAnyStubTest
 */

public class _DynAnyStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny._DynAnyStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "implements org.omg.DynamicAny.DynAny");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
