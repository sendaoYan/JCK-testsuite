/*
 * @test
 * @summary org.omg.CORBA_2_3.portable.ObjectImpl conformance: abstract class shape.
 * @run main ObjectImplTest
 */

public class ObjectImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA_2_3.portable.ObjectImpl");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        // ---- deepened (jdk8u372-b07 probed): 2.3 ObjectImpl codebase face ----
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == org.omg.CORBA.portable.ObjectImpl.class, "extends org.omg.CORBA.portable.ObjectImpl");
        check(c.getDeclaredMethods().length == 1, "exactly one added method");
        java.lang.reflect.Method gc = c.getDeclaredMethod("_get_codebase");
        check(gc.getReturnType() == String.class, "_get_codebase() returns String");
        check(java.lang.reflect.Modifier.isPublic(gc.getModifiers()) && !java.lang.reflect.Modifier.isStatic(gc.getModifiers()),
              "_get_codebase is a public instance method");
        // real usage: org.omg.stub ties extend this class
        check(org.omg.CORBA_2_3.portable.ObjectImpl.class.isAssignableFrom(
                  org.omg.stub.javax.management.remote.rmi._RMIConnectionImpl_Tie.class),
              "the JMX RMI tie classes extend org.omg.CORBA_2_3.portable.ObjectImpl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
