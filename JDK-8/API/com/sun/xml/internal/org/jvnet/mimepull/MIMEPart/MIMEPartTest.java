/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.mimepull.MIMEPart conformance: class hierarchy and members.
 * @run main MIMEPartTest
 */

public class MIMEPartTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.mimepull.MIMEPart", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.mimepull.MIMEPart"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addBody"), "has method addBody");
        check(mn.contains("close"), "has method close");
        check(mn.contains("doneParsing"), "has method doneParsing");
        check(mn.contains("getAllHeaders"), "has method getAllHeaders");
        check(mn.contains("getContentId"), "has method getContentId");
        check(mn.contains("getContentTransferEncoding"), "has method getContentTransferEncoding");
        check(mn.contains("getContentType"), "has method getContentType");
        check(mn.contains("getHeader"), "has method getHeader");
        check(mn.contains("getHeaders"), "has method getHeaders");
        check(mn.contains("moveTo"), "has method moveTo");
        check(mn.contains("read"), "has method read");
        check(mn.contains("readOnce"), "has method readOnce");
        check(mn.contains("setContentId"), "has method setContentId");
        check(mn.contains("setContentTransferEncoding"), "has method setContentTransferEncoding");
        check(mn.contains("setHeaders"), "has method setHeaders");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
