/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.FastInfosetReflection conformance: class hierarchy and members.
 * @run main FastInfosetReflectionTest
 */

public class FastInfosetReflectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.FastInfosetReflection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.FastInfosetReflection"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("DOMDocumentParser_new"), "has method DOMDocumentParser_new");
        check(mn.contains("DOMDocumentParser_parse"), "has method DOMDocumentParser_parse");
        check(mn.contains("DOMDocumentSerializer_new"), "has method DOMDocumentSerializer_new");
        check(mn.contains("DOMDocumentSerializer_serialize"), "has method DOMDocumentSerializer_serialize");
        check(mn.contains("DOMDocumentSerializer_setOutputStream"), "has method DOMDocumentSerializer_setOutputStream");
        check(mn.contains("FastInfosetResult_getOutputStream"), "has method FastInfosetResult_getOutputStream");
        check(mn.contains("FastInfosetResult_new"), "has method FastInfosetResult_new");
        check(mn.contains("FastInfosetSource_getInputStream"), "has method FastInfosetSource_getInputStream");
        check(mn.contains("FastInfosetSource_new"), "has method FastInfosetSource_new");
        check(mn.contains("FastInfosetSource_setInputStream"), "has method FastInfosetSource_setInputStream");
        check(mn.contains("getFastInfosetSource_class"), "has method getFastInfosetSource_class");
        check(mn.contains("isFastInfosetResult"), "has method isFastInfosetResult");
        check(mn.contains("isFastInfosetSource"), "has method isFastInfosetSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
