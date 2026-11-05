/*
 * @test
 * @summary org.omg.PortableInterceptor.RequestInfoOperations conformance: interface shape.
 * @run main RequestInfoOperationsTest
 */

public class RequestInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.RequestInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): full 14-method info face ----
        java.util.Set<String> requestinfooperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.RequestInfoOperations.class.getDeclaredMethods()) requestinfooperations_m.add(m.getName());
        check(requestinfooperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("request_id", "operation", "arguments", "exceptions", "contexts", "operation_context", "result", "response_expected", "sync_scope", "reply_status", "forward_reference", "get_slot", "get_request_service_context", "get_reply_service_context"))), "method-name set exact (14 methods)");
        check(org.omg.PortableInterceptor.RequestInfoOperations.class.getDeclaredMethods().length == 14, "declared method count == 14");
        java.lang.reflect.Method rs = org.omg.PortableInterceptor.RequestInfoOperations.class.getDeclaredMethod("reply_status");
        check(rs.getReturnType() == short.class, "reply_status() returns short (ReplyStatus family)");
        java.lang.reflect.Method ss = org.omg.PortableInterceptor.RequestInfoOperations.class.getDeclaredMethod("sync_scope");
        check(ss.getReturnType() == short.class, "sync_scope() returns short (Messaging::SyncScope family)");
        java.lang.reflect.Method gs = org.omg.PortableInterceptor.RequestInfoOperations.class.getDeclaredMethod("get_slot", int.class);
        check(gs.getExceptionTypes()[0] == org.omg.PortableInterceptor.InvalidSlot.class,
              "get_slot throws InvalidSlot");
        java.lang.reflect.Method grsc = org.omg.PortableInterceptor.RequestInfoOperations.class.getDeclaredMethod(
            "get_request_service_context", int.class);
        check(grsc.getReturnType() == org.omg.IOP.ServiceContext.class,
              "get_request_service_context returns org.omg.IOP.ServiceContext (cross-module type)");
        java.lang.reflect.Method fr = org.omg.PortableInterceptor.RequestInfoOperations.class.getDeclaredMethod("forward_reference");
        check(fr.getReturnType() == org.omg.CORBA.Object.class, "forward_reference() returns CORBA Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
