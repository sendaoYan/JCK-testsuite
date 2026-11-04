/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart conformance: class hierarchy and members.
 * @run main MimeMultipartTest
 */

public class MimeMultipartTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addBodyPart"), "has method addBodyPart");
        check(mn.contains("createInternetHeaders"), "has method createInternetHeaders");
        check(mn.contains("createMimeBodyPart"), "has method createMimeBodyPart");
        check(mn.contains("getBodyPart"), "has method getBodyPart");
        check(mn.contains("getContentType"), "has method getContentType");
        check(mn.contains("getCount"), "has method getCount");
        check(mn.contains("getParent"), "has method getParent");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("removeBodyPart"), "has method removeBodyPart");
        check(mn.contains("setMultipartDataSource"), "has method setMultipartDataSource");
        check(mn.contains("setParent"), "has method setParent");
        check(mn.contains("setSubType"), "has method setSubType");
        check(mn.contains("updateHeaders"), "has method updateHeaders");
        check(mn.contains("writeTo"), "has method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
