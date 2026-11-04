/*
 * @test
 * @summary sun.reflect.annotation.TypeAnnotationParser conformance: class and members.
 * @run main TypeAnnotationParserTest
 */

public class TypeAnnotationParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeAnnotationParser", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.TypeAnnotationParser"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("buildAnnotatedInterfaces"), "declares method buildAnnotatedInterfaces");
        check(mnames.contains("buildAnnotatedSuperclass"), "declares method buildAnnotatedSuperclass");
        check(mnames.contains("buildAnnotatedType"), "declares method buildAnnotatedType");
        check(mnames.contains("buildAnnotatedTypes"), "declares method buildAnnotatedTypes");
        check(mnames.contains("fetchBounds"), "declares method fetchBounds");
        check(mnames.contains("mapTypeAnnotations"), "declares method mapTypeAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
