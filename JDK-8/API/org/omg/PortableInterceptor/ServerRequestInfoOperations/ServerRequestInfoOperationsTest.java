/*
 * @test
 * @summary org.omg.PortableInterceptor.ServerRequestInfoOperations conformance: interface shape.
 * @run main ServerRequestInfoOperationsTest
 */

public class ServerRequestInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ServerRequestInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.RequestInfoOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.RequestInfoOperations");
        // ---- deepened (jdk8u372-b07 probed): full 11-method server info face ----
        java.util.Set<String> serverrequestinfooperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ServerRequestInfoOperations.class.getDeclaredMethods()) serverrequestinfooperations_m.add(m.getName());
        check(serverrequestinfooperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("sending_exception", "object_id", "adapter_id", "server_id", "orb_id", "adapter_name", "target_most_derived_interface", "get_server_policy", "set_slot", "target_is_a", "add_reply_service_context"))), "method-name set exact (11 methods)");
        check(org.omg.PortableInterceptor.ServerRequestInfoOperations.class.getDeclaredMethods().length == 11, "declared method count == 11");
        java.lang.reflect.Method oi = org.omg.PortableInterceptor.ServerRequestInfoOperations.class.getDeclaredMethod("object_id");
        check(oi.getReturnType() == byte[].class, "object_id() returns byte[]");
        java.lang.reflect.Method an = org.omg.PortableInterceptor.ServerRequestInfoOperations.class.getDeclaredMethod("adapter_name");
        check(an.getReturnType() == String[].class, "adapter_name() returns String[]");
        java.lang.reflect.Method ss = org.omg.PortableInterceptor.ServerRequestInfoOperations.class.getDeclaredMethod(
            "set_slot", int.class, org.omg.CORBA.Any.class);
        check(ss.getExceptionTypes()[0] == org.omg.PortableInterceptor.InvalidSlot.class, "set_slot throws InvalidSlot");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
