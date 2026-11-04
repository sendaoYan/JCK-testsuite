/*
 * @test
 * @summary sun.reflect.generics.tree.MethodTypeSignature conformance: class and members.
 * @run main MethodTypeSignatureTest
 */

public class MethodTypeSignatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.tree.MethodTypeSignature", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.tree.MethodTypeSignature"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.reflect.generics.tree.Signature"), "interface sun.reflect.generics.tree.Signature");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("accept"), "declares method accept");
        check(mnames.contains("getExceptionTypes"), "declares method getExceptionTypes");
        check(mnames.contains("getFormalTypeParameters"), "declares method getFormalTypeParameters");
        check(mnames.contains("getParameterTypes"), "declares method getParameterTypes");
        check(mnames.contains("getReturnType"), "declares method getReturnType");
        check(mnames.contains("make"), "declares method make");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
