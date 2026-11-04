/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader conformance: interface hierarchy and members.
 * @run main AnnotationReaderTest
 */

public class AnnotationReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAllFieldAnnotations"), "has method getAllFieldAnnotations");
        check(mn.contains("getAllMethodAnnotations"), "has method getAllMethodAnnotations");
        check(mn.contains("getClassAnnotation"), "has method getClassAnnotation");
        check(mn.contains("getClassArrayValue"), "has method getClassArrayValue");
        check(mn.contains("getClassValue"), "has method getClassValue");
        check(mn.contains("getFieldAnnotation"), "has method getFieldAnnotation");
        check(mn.contains("getMethodAnnotation"), "has method getMethodAnnotation");
        check(mn.contains("getMethodParameterAnnotation"), "has method getMethodParameterAnnotation");
        check(mn.contains("getPackageAnnotation"), "has method getPackageAnnotation");
        check(mn.contains("hasClassAnnotation"), "has method hasClassAnnotation");
        check(mn.contains("hasFieldAnnotation"), "has method hasFieldAnnotation");
        check(mn.contains("hasMethodAnnotation"), "has method hasMethodAnnotation");
        check(mn.contains("setErrorHandler"), "has method setErrorHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
