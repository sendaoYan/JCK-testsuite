/*
 * @test
 * @summary org.omg.PortableInterceptor.ClientRequestInfoOperations conformance: interface shape.
 * @run main ClientRequestInfoOperationsTest
 */

public class ClientRequestInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ClientRequestInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.RequestInfoOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.RequestInfoOperations");
        // ---- deepened (jdk8u372-b07 probed): full 9-method client info face ----
        java.util.Set<String> clientrequestinfooperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ClientRequestInfoOperations.class.getDeclaredMethods()) clientrequestinfooperations_m.add(m.getName());
        check(clientrequestinfooperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("target", "effective_target", "effective_profile", "received_exception", "received_exception_id", "get_effective_component", "get_effective_components", "get_request_policy", "add_request_service_context"))), "method-name set exact (9 methods)");
        check(org.omg.PortableInterceptor.ClientRequestInfoOperations.class.getDeclaredMethods().length == 9, "declared method count == 9");
        java.lang.reflect.Method ep = org.omg.PortableInterceptor.ClientRequestInfoOperations.class.getDeclaredMethod("effective_profile");
        check(ep.getReturnType() == org.omg.IOP.TaggedProfile.class, "effective_profile() returns IOP.TaggedProfile");
        java.lang.reflect.Method gec = org.omg.PortableInterceptor.ClientRequestInfoOperations.class.getDeclaredMethod(
            "get_effective_component", int.class);
        check(gec.getReturnType() == org.omg.IOP.TaggedComponent.class,
              "get_effective_component(int) returns IOP.TaggedComponent");
        java.lang.reflect.Method rec = org.omg.PortableInterceptor.ClientRequestInfoOperations.class.getDeclaredMethod(
            "received_exception_id");
        check(rec.getReturnType() == String.class, "received_exception_id() returns String repository id");
        java.lang.reflect.Method ars = org.omg.PortableInterceptor.ClientRequestInfoOperations.class.getDeclaredMethod(
            "add_request_service_context", org.omg.IOP.ServiceContext.class, boolean.class);
        check(ars.getReturnType() == void.class, "add_request_service_context(ServiceContext, boolean) void");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
