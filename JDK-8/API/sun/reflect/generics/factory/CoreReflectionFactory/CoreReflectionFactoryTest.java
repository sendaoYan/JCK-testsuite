/*
 * @test
 * @summary sun.reflect.generics.factory.CoreReflectionFactory conformance: class and members.
 * @run main CoreReflectionFactoryTest
 */

public class CoreReflectionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.factory.CoreReflectionFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.factory.CoreReflectionFactory"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.reflect.generics.factory.GenericsFactory"), "interface sun.reflect.generics.factory.GenericsFactory");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("findTypeVariable"), "declares method findTypeVariable");
        check(mnames.contains("getDecl"), "declares method getDecl");
        check(mnames.contains("getDeclsLoader"), "declares method getDeclsLoader");
        check(mnames.contains("getScope"), "declares method getScope");
        check(mnames.contains("make"), "declares method make");
        check(mnames.contains("makeArrayType"), "declares method makeArrayType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
