/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl conformance: abstract class hierarchy and members.
 * @run main AbstractInlineAnnotationReaderImplTest
 */

public class AbstractInlineAnnotationReaderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader"), "has interface com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("fullName"), "has method fullName");
        check(mn.contains("getErrorHandler"), "has method getErrorHandler");
        check(mn.contains("getMethodAnnotation"), "has method getMethodAnnotation");
        check(mn.contains("hasMethodAnnotation"), "has method hasMethodAnnotation");
        check(mn.contains("setErrorHandler"), "has method setErrorHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
