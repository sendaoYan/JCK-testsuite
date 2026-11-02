/*
 * @test
 * @summary sun.reflect.annotation.TypeNotPresentExceptionProxy conformance: class and members.
 * @run main TypeNotPresentExceptionProxyTest
 */

public class TypeNotPresentExceptionProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeNotPresentExceptionProxy", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.TypeNotPresentExceptionProxy"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.annotation.ExceptionProxy"), "superclass sun.reflect.annotation.ExceptionProxy");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("generateException"), "declares method generateException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
