/*
 * @test
 * @summary org.omg.CORBA.portable.InvokeHandler conformance: interface shape.
 * @run main InvokeHandlerTest
 */

public class InvokeHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.InvokeHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact _invoke surface ----
        check(c.getDeclaredMethods().length == 1, "single method interface");
        java.lang.reflect.Method inv = c.getDeclaredMethods()[0];
        check("_invoke".equals(inv.getName()), "method is _invoke");
        Class<?>[] pts = inv.getParameterTypes();
        check(pts.length == 3 && pts[0] == String.class
              && pts[1] == org.omg.CORBA.portable.InputStream.class
              && pts[2] == org.omg.CORBA.portable.ResponseHandler.class,
              "_invoke(String, InputStream, ResponseHandler)");
        check(inv.getReturnType() == org.omg.CORBA.portable.OutputStream.class, "_invoke returns OutputStream");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
