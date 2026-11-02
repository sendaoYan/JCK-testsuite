/*
 * @test
 * @summary sun.tracing.ProviderSkeleton conformance: abstract class and members.
 * @run main ProviderSkeletonTest
 */

public class ProviderSkeletonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tracing.ProviderSkeleton", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.tracing.ProviderSkeleton"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.reflect.InvocationHandler"), "interface java.lang.reflect.InvocationHandler");
        check(ifaces.contains("com.sun.tracing.Provider"), "interface com.sun.tracing.Provider");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createProbe"), "declares method createProbe");
        check(mnames.contains("dispose"), "declares method dispose");
        check(mnames.contains("getAnnotationString"), "declares method getAnnotationString");
        check(mnames.contains("getAnnotationValue"), "declares method getAnnotationValue");
        check(mnames.contains("getProbe"), "declares method getProbe");
        check(mnames.contains("getProviderName"), "declares method getProviderName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
