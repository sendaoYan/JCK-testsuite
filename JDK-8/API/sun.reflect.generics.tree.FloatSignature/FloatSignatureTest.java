/*
 * @test
 * @summary sun.reflect.generics.tree.FloatSignature conformance: class and members.
 * @run main FloatSignatureTest
 */

public class FloatSignatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.tree.FloatSignature", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.tree.FloatSignature"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.reflect.generics.tree.BaseType"), "interface sun.reflect.generics.tree.BaseType");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("accept"), "declares method accept");
        check(mnames.contains("make"), "declares method make");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
