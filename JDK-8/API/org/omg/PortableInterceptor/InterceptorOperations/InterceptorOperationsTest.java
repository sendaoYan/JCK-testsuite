/*
 * @test
 * @summary org.omg.PortableInterceptor.InterceptorOperations conformance: interface shape.
 * @run main InterceptorOperationsTest
 */

public class InterceptorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.InterceptorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): lifecycle method face ----
        java.util.Set<String> interceptoroperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.InterceptorOperations.class.getDeclaredMethods()) interceptoroperations_m.add(m.getName());
        check(interceptoroperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("name", "destroy"))), "method-name set exact (2 methods)");
        check(org.omg.PortableInterceptor.InterceptorOperations.class.getDeclaredMethods().length == 2, "declared method count == 2");
        java.lang.reflect.Method nm = org.omg.PortableInterceptor.InterceptorOperations.class.getDeclaredMethod("name");
        check(nm.getReturnType() == String.class, "name() returns String");
        check(java.lang.reflect.Modifier.isAbstract(nm.getModifiers()), "name() abstract");
        java.lang.reflect.Method dy = org.omg.PortableInterceptor.InterceptorOperations.class.getDeclaredMethod("destroy");
        check(dy.getReturnType() == void.class && dy.getExceptionTypes().length == 0,
              "destroy() returns void and declares no checked exceptions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
