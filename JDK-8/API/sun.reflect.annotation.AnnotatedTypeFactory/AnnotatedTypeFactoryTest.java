/*
 * @test
 * @summary sun.reflect.annotation.AnnotatedTypeFactory conformance: class and members.
 * @run main AnnotatedTypeFactoryTest
 */

public class AnnotatedTypeFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.AnnotatedTypeFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.AnnotatedTypeFactory"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addNesting"), "declares method addNesting");
        check(mnames.contains("buildAnnotatedType"), "declares method buildAnnotatedType");
        check(mnames.contains("isArray"), "declares method isArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
