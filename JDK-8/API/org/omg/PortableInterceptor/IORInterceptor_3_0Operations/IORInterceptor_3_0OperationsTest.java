/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInterceptor_3_0Operations conformance: interface shape.
 * @run main IORInterceptor_3_0OperationsTest
 */

public class IORInterceptor_3_0OperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.IORInterceptor_3_0Operations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.IORInterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.IORInterceptorOperations");
        // ---- deepened (jdk8u372-b07 probed): 3.0 hooks ----
        java.util.Set<String> iorinterceptor_3_0operations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.IORInterceptor_3_0Operations.class.getDeclaredMethods()) iorinterceptor_3_0operations_m.add(m.getName());
        check(iorinterceptor_3_0operations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("components_established", "adapter_manager_state_changed", "adapter_state_changed"))), "method-name set exact (3 methods)");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Operations.class.getDeclaredMethods().length == 3, "declared method count == 3");
        java.lang.reflect.Method ams = org.omg.PortableInterceptor.IORInterceptor_3_0Operations.class.getDeclaredMethod(
            "adapter_manager_state_changed", int.class, short.class);
        check(ams != null, "adapter_manager_state_changed(int, short) exact primitives");
        java.lang.reflect.Method asc = org.omg.PortableInterceptor.IORInterceptor_3_0Operations.class.getDeclaredMethod(
            "adapter_state_changed", org.omg.PortableInterceptor.ObjectReferenceTemplate[].class, short.class);
        check(asc != null, "adapter_state_changed(ObjectReferenceTemplate[], short) exact");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
