/*
 * @test
 * @summary org.omg.DynamicAny._DynUnionStub conformance: stub/tie class shape.
 * @run main _DynUnionStubTest
 */

public class _DynUnionStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny._DynUnionStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.DynamicAny.DynUnion.class.isAssignableFrom(c), "implements org.omg.DynamicAny.DynUnion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
