/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoOperations conformance: interface shape.
 * @run main ORBInitInfoOperationsTest
 */

public class ORBInitInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ORBInitInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): full 10-method registration face ----
        java.util.Set<String> orbinitinfooperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ORBInitInfoOperations.class.getDeclaredMethods()) orbinitinfooperations_m.add(m.getName());
        check(orbinitinfooperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("arguments", "orb_id", "codec_factory", "register_initial_reference", "resolve_initial_references", "add_client_request_interceptor", "add_server_request_interceptor", "add_ior_interceptor", "allocate_slot_id", "register_policy_factory"))), "method-name set exact (10 methods)");
        check(org.omg.PortableInterceptor.ORBInitInfoOperations.class.getDeclaredMethods().length == 10, "declared method count == 10");
        java.lang.reflect.Method cf = org.omg.PortableInterceptor.ORBInitInfoOperations.class.getDeclaredMethod("codec_factory");
        check(cf.getReturnType() == org.omg.IOP.CodecFactory.class, "codec_factory() returns org.omg.IOP.CodecFactory");
        java.lang.reflect.Method asi = org.omg.PortableInterceptor.ORBInitInfoOperations.class.getDeclaredMethod(
            "allocate_slot_id");
        check(asi.getReturnType() == int.class, "allocate_slot_id() returns int");
        java.lang.reflect.Method acr = org.omg.PortableInterceptor.ORBInitInfoOperations.class.getDeclaredMethod(
            "add_client_request_interceptor", org.omg.PortableInterceptor.ClientRequestInterceptor.class);
        check(acr.getExceptionTypes()[0] == org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName.class,
              "add_client_request_interceptor throws ORBInitInfoPackage.DuplicateName");
        java.lang.reflect.Method rir = org.omg.PortableInterceptor.ORBInitInfoOperations.class.getDeclaredMethod(
            "resolve_initial_references", String.class);
        check(rir.getExceptionTypes()[0] == org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName.class,
              "resolve_initial_references throws ORBInitInfoPackage.InvalidName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
