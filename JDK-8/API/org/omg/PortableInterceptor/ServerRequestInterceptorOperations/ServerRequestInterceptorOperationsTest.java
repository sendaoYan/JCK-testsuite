/*
 * @test
 * @summary org.omg.PortableInterceptor.ServerRequestInterceptorOperations conformance: interface shape.
 * @run main ServerRequestInterceptorOperationsTest
 */

public class ServerRequestInterceptorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ServerRequestInterceptorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.InterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.InterceptorOperations");
        // ---- deepened (jdk8u372-b07 probed): exactly the 5 server hooks ----
        java.util.Set<String> serverrequestinterceptoroperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ServerRequestInterceptorOperations.class.getDeclaredMethods()) serverrequestinterceptoroperations_m.add(m.getName());
        check(serverrequestinterceptoroperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("receive_request_service_contexts", "receive_request", "send_reply", "send_exception", "send_other"))), "method-name set exact (5 methods)");
        check(org.omg.PortableInterceptor.ServerRequestInterceptorOperations.class.getDeclaredMethods().length == 5, "declared method count == 5");
        java.lang.reflect.Method rr = org.omg.PortableInterceptor.ServerRequestInterceptorOperations.class.getDeclaredMethod(
            "receive_request", org.omg.PortableInterceptor.ServerRequestInfo.class);
        check(rr.getExceptionTypes()[0] == org.omg.PortableInterceptor.ForwardRequest.class,
              "receive_request declares ForwardRequest");
        java.lang.reflect.Method srl = org.omg.PortableInterceptor.ServerRequestInterceptorOperations.class.getDeclaredMethod(
            "send_reply", org.omg.PortableInterceptor.ServerRequestInfo.class);
        check(srl.getExceptionTypes().length == 0, "send_reply declares no exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
