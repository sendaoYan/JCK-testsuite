/*
 * @test
 * @summary sun.reflect.annotation.AnnotationType conformance: class and members.
 * @run main AnnotationTypeTest
 */

public class AnnotationTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.AnnotationType", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.AnnotationType"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getInstance"), "declares method getInstance");
        check(mnames.contains("invocationHandlerReturnType"), "declares method invocationHandlerReturnType");
        check(mnames.contains("isInherited"), "declares method isInherited");
        check(mnames.contains("memberDefaults"), "declares method memberDefaults");
        check(mnames.contains("memberTypes"), "declares method memberTypes");
        check(mnames.contains("members"), "declares method members");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
