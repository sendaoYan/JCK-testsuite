/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitializerOperations conformance: interface shape.
 * @run main ORBInitializerOperationsTest
 */

public class ORBInitializerOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ORBInitializerOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): init hooks ----
        java.util.Set<String> orbinitializeroperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.ORBInitializerOperations.class.getDeclaredMethods()) orbinitializeroperations_m.add(m.getName());
        check(orbinitializeroperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("pre_init", "post_init"))), "method-name set exact (2 methods)");
        check(org.omg.PortableInterceptor.ORBInitializerOperations.class.getDeclaredMethods().length == 2, "declared method count == 2");
        java.lang.reflect.Method pre = org.omg.PortableInterceptor.ORBInitializerOperations.class.getDeclaredMethod(
            "pre_init", org.omg.PortableInterceptor.ORBInitInfo.class);
        check(pre.getReturnType() == void.class && pre.getExceptionTypes().length == 0,
              "pre_init(ORBInitInfo) void no checked exceptions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
