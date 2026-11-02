/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.annotation.RuntimeInlineAnnotationReader conformance: class hierarchy and members.
 * @run main RuntimeInlineAnnotationReaderTest
 */

public class RuntimeInlineAnnotationReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.annotation.RuntimeInlineAnnotationReader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.annotation.RuntimeInlineAnnotationReader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader"), "has interface com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("fullName"), "has method fullName");
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
