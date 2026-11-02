/*
 * @test
 * @summary sun.reflect.annotation.AnnotationParser conformance: class and members.
 * @run main AnnotationParserTest
 */

public class AnnotationParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.AnnotationParser", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.AnnotationParser"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("annotationForMap"), "declares method annotationForMap");
        check(mnames.contains("contains"), "declares method contains");
        check(mnames.contains("exceptionProxy"), "declares method exceptionProxy");
        check(mnames.contains("getEmptyAnnotationArray"), "declares method getEmptyAnnotationArray");
        check(mnames.contains("parseAnnotation"), "declares method parseAnnotation");
        check(mnames.contains("parseAnnotation2"), "declares method parseAnnotation2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
