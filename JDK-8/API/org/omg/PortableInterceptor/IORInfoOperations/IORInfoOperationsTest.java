/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInfoOperations conformance: interface shape.
 * @run main IORInfoOperationsTest
 */

public class IORInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.IORInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): full 8-method face (current_factory is get+set) ----
        java.util.Set<String> iorinfooperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.IORInfoOperations.class.getDeclaredMethods()) iorinfooperations_m.add(m.getName());
        check(iorinfooperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("get_effective_policy", "add_ior_component", "add_ior_component_to_profile", "manager_id", "state", "adapter_template", "current_factory"))), "method-name set exact (8 methods)");
        check(org.omg.PortableInterceptor.IORInfoOperations.class.getDeclaredMethods().length == 8, "declared method count == 8");
        java.lang.reflect.Method at = org.omg.PortableInterceptor.IORInfoOperations.class.getDeclaredMethod("adapter_template");
        check(at.getReturnType() == org.omg.PortableInterceptor.ObjectReferenceTemplate.class,
              "adapter_template() returns ObjectReferenceTemplate");
        java.lang.reflect.Method cfs = org.omg.PortableInterceptor.IORInfoOperations.class.getDeclaredMethod(
            "current_factory", org.omg.PortableInterceptor.ObjectReferenceFactory.class);
        check(cfs.getReturnType() == void.class, "current_factory(ObjectReferenceFactory) setter returns void");
        java.lang.reflect.Method st = org.omg.PortableInterceptor.IORInfoOperations.class.getDeclaredMethod("state");
        check(st.getReturnType() == short.class, "state() returns short (adapter state family)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
