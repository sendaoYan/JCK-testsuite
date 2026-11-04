/*
 * @test
 * @summary com.sun.xml.internal.stream.Entity.ScannedEntity conformance: class hierarchy and members.
 * @run main ScannedEntityTest
 */

public class ScannedEntityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.Entity$ScannedEntity", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.Entity$ScannedEntity"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.Entity".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.Entity");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("getEncodingName"), "has method getEncodingName");
        check(mn.contains("getEntityInputStream"), "has method getEntityInputStream");
        check(mn.contains("getEntityReader"), "has method getEntityReader");
        check(mn.contains("getEntityVersion"), "has method getEntityVersion");
        check(mn.contains("isDeclaredEncoding"), "has method isDeclaredEncoding");
        check(mn.contains("isEncodingExternallySpecified"), "has method isEncodingExternallySpecified");
        check(mn.contains("isExternal"), "has method isExternal");
        check(mn.contains("isUnparsed"), "has method isUnparsed");
        check(mn.contains("setDeclaredEncoding"), "has method setDeclaredEncoding");
        check(mn.contains("setEncodingExternallySpecified"), "has method setEncodingExternallySpecified");
        check(mn.contains("setEntityVersion"), "has method setEntityVersion");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
