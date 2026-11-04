/*
 * @test
 * @summary sun.instrument.InstrumentationImpl conformance: class and members.
 * @run main InstrumentationImplTest
 */

public class InstrumentationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.instrument.InstrumentationImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.instrument.InstrumentationImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.instrument.Instrumentation"), "interface java.lang.instrument.Instrumentation");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addTransformer"), "declares method addTransformer");
        check(mnames.contains("appendToBootstrapClassLoaderSearch"), "declares method appendToBootstrapClassLoaderSearch");
        check(mnames.contains("appendToClassLoaderSearch0"), "declares method appendToClassLoaderSearch0");
        check(mnames.contains("appendToSystemClassLoaderSearch"), "declares method appendToSystemClassLoaderSearch");
        check(mnames.contains("findTransformerManager"), "declares method findTransformerManager");
        check(mnames.contains("getAllLoadedClasses"), "declares method getAllLoadedClasses");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
