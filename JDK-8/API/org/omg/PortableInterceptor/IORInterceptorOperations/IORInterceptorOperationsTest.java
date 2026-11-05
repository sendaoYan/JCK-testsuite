/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInterceptorOperations conformance: interface shape.
 * @run main IORInterceptorOperationsTest
 */

public class IORInterceptorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.IORInterceptorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.InterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.InterceptorOperations");
        // ---- deepened (jdk8u372-b07 probed): single hook ----
        java.util.Set<String> iorinterceptoroperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.IORInterceptorOperations.class.getDeclaredMethods()) iorinterceptoroperations_m.add(m.getName());
        check(iorinterceptoroperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("establish_components"))), "method-name set exact (1 methods)");
        check(org.omg.PortableInterceptor.IORInterceptorOperations.class.getDeclaredMethods().length == 1, "declared method count == 1");
        java.lang.reflect.Method ec = org.omg.PortableInterceptor.IORInterceptorOperations.class.getDeclaredMethod(
            "establish_components", org.omg.PortableInterceptor.IORInfo.class);
        check(ec.getReturnType() == void.class && ec.getExceptionTypes().length == 0,
              "establish_components(IORInfo) void, no checked exceptions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
