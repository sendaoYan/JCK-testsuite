/*
 * @test
 * @summary org.omg.CORBA.portable.ResponseHandler conformance: interface shape.
 * @run main ResponseHandlerTest
 */

public class ResponseHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.ResponseHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact 2-method surface ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("createReply", "createExceptionReply"))),
              "ResponseHandler declares exactly createReply/createExceptionReply: " + names);
        check(c.getMethod("createReply").getReturnType() == org.omg.CORBA.portable.OutputStream.class,
              "createReply returns OutputStream");
        check(c.getMethod("createExceptionReply").getReturnType() == org.omg.CORBA.portable.OutputStream.class,
              "createExceptionReply returns OutputStream");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
