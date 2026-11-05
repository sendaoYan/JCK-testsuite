/*
 * @test
 * @summary org.omg.CORBA.portable.ValueFactory conformance: interface shape.
 * @run main ValueFactoryTest
 */

public class ValueFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.ValueFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact 1-method surface ----
        check(c.getDeclaredMethods().length == 1, "single method interface");
        java.lang.reflect.Method rv = c.getDeclaredMethods()[0];
        check("read_value".equals(rv.getName()), "method is read_value");
        check(rv.getParameterTypes().length == 1
              && rv.getParameterTypes()[0] == org.omg.CORBA_2_3.portable.InputStream.class,
              "read_value takes org.omg.CORBA_2_3.portable.InputStream (jdk8u372)");
        check(rv.getReturnType() == java.io.Serializable.class, "read_value returns Serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
