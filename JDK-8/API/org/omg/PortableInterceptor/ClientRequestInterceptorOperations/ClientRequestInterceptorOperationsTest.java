/*
 * @test
 * @summary org.omg.PortableInterceptor.ClientRequestInterceptorOperations conformance: interface shape.
 * @run main ClientRequestInterceptorOperationsTest
 */

public class ClientRequestInterceptorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ClientRequestInterceptorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.InterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.InterceptorOperations");
        // ---- deepened (jdk8u372-b07 probed): exactly the 5 client hooks ----
        java.util.Set<String> clientrequestinterceptoroperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ClientRequestInterceptorOperations.class.getDeclaredMethods()) clientrequestinterceptoroperations_m.add(m.getName());
        check(clientrequestinterceptoroperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("send_request", "send_poll", "receive_reply", "receive_exception", "receive_other"))), "method-name set exact (5 methods)");
        check(org.omg.PortableInterceptor.ClientRequestInterceptorOperations.class.getDeclaredMethods().length == 5, "declared method count == 5");
        java.lang.reflect.Method sr = org.omg.PortableInterceptor.ClientRequestInterceptorOperations.class.getDeclaredMethod(
            "send_request", org.omg.PortableInterceptor.ClientRequestInfo.class);
        check(sr.getExceptionTypes()[0] == org.omg.PortableInterceptor.ForwardRequest.class,
              "send_request declares ForwardRequest");
        java.lang.reflect.Method sp = org.omg.PortableInterceptor.ClientRequestInterceptorOperations.class.getDeclaredMethod(
            "send_poll", org.omg.PortableInterceptor.ClientRequestInfo.class);
        check(sp.getExceptionTypes().length == 0, "send_poll declares no exception");
        java.lang.reflect.Method re = org.omg.PortableInterceptor.ClientRequestInterceptorOperations.class.getDeclaredMethod(
            "receive_exception", org.omg.PortableInterceptor.ClientRequestInfo.class);
        check(re.getExceptionTypes().length == 1 && re.getExceptionTypes()[0] == org.omg.PortableInterceptor.ForwardRequest.class,
              "receive_exception declares ForwardRequest only");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
