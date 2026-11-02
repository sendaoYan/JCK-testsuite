/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.IllegalAnnotationException conformance: class hierarchy and members.
 * @run main IllegalAnnotationExceptionTest
 */

public class IllegalAnnotationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.IllegalAnnotationException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.IllegalAnnotationException"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.bind.JAXBException".equals(c.getSuperclass().getName()), "extends javax.xml.bind.JAXBException");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("build"), "has method build");
        check(mn.contains("cast"), "has method cast");
        check(mn.contains("convert"), "has method convert");
        check(mn.contains("getSourcePos"), "has method getSourcePos");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
