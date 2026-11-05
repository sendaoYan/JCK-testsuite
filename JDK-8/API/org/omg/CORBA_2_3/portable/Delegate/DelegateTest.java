/*
 * @test
 * @summary org.omg.CORBA_2_3.portable.Delegate conformance: abstract class shape.
 * @run main DelegateTest
 */

public class DelegateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA_2_3.portable.Delegate");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.portable.Delegate.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.Delegate");
        // ---- deepened (jdk8u372-b07 probed): 2.3 Delegate codebase face ----
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == org.omg.CORBA.portable.Delegate.class, "extends org.omg.CORBA.portable.Delegate");
        check(c.getDeclaredMethods().length == 1, "exactly one added method");
        java.lang.reflect.Method gc = c.getDeclaredMethod("get_codebase", org.omg.CORBA.Object.class);
        check(gc.getReturnType() == String.class, "get_codebase(org.omg.CORBA.Object) returns String");
        check(!java.lang.reflect.Modifier.isAbstract(gc.getModifiers()), "get_codebase is concrete (overridable)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
