/*
 * @test
 * @summary sun.tracing.ProbeSkeleton conformance: abstract class and members.
 * @run main ProbeSkeletonTest
 */

public class ProbeSkeletonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tracing.ProbeSkeleton", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.tracing.ProbeSkeleton"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("com.sun.tracing.Probe"), "interface com.sun.tracing.Probe");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("isAssignable"), "declares method isAssignable");
        check(mnames.contains("isEnabled"), "declares method isEnabled");
        check(mnames.contains("trigger"), "declares method trigger");
        check(mnames.contains("uncheckedTrigger"), "declares method uncheckedTrigger");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
