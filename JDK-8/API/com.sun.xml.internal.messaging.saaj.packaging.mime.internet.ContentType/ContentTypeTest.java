/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ContentType conformance: class hierarchy and members.
 * @run main ContentTypeTest
 */

public class ContentTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ContentType", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ContentType"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("copy"), "has method copy");
        check(mn.contains("getBaseType"), "has method getBaseType");
        check(mn.contains("getParameter"), "has method getParameter");
        check(mn.contains("getParameterList"), "has method getParameterList");
        check(mn.contains("getPrimaryType"), "has method getPrimaryType");
        check(mn.contains("getSubType"), "has method getSubType");
        check(mn.contains("match"), "has method match");
        check(mn.contains("setParameter"), "has method setParameter");
        check(mn.contains("setParameterList"), "has method setParameterList");
        check(mn.contains("setPrimaryType"), "has method setPrimaryType");
        check(mn.contains("setSubType"), "has method setSubType");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
