/*
 * @test
 * @summary sun.reflect.annotation.ExceptionProxy conformance: abstract class and members.
 * @run main ExceptionProxyTest
 */

public class ExceptionProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.ExceptionProxy", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.ExceptionProxy"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.Serializable"), "interface java.io.Serializable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("generateException"), "declares method generateException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
