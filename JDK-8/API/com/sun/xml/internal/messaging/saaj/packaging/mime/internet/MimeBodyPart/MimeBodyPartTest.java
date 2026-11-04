/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeBodyPart conformance: class hierarchy and members.
 * @run main MimeBodyPartTest
 */

public class MimeBodyPartTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeBodyPart", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeBodyPart"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addHeader"), "has method addHeader");
        check(mn.contains("addHeaderLine"), "has method addHeaderLine");
        check(mn.contains("getAllHeaders"), "has method getAllHeaders");
        check(mn.contains("getContent"), "has method getContent");
        check(mn.contains("getContentID"), "has method getContentID");
        check(mn.contains("getContentLanguage"), "has method getContentLanguage");
        check(mn.contains("getContentMD5"), "has method getContentMD5");
        check(mn.contains("getContentStream"), "has method getContentStream");
        check(mn.contains("getContentType"), "has method getContentType");
        check(mn.contains("getDataHandler"), "has method getDataHandler");
        check(mn.contains("getDescription"), "has method getDescription");
        check(mn.contains("getDisposition"), "has method getDisposition");
        check(mn.contains("getEncoding"), "has method getEncoding");
        check(mn.contains("getFileName"), "has method getFileName");
        check(mn.contains("getHeader"), "has method getHeader");
        check(mn.contains("getInputStream"), "has method getInputStream");
        check(mn.contains("getLineCount"), "has method getLineCount");
        check(mn.contains("getParent"), "has method getParent");
        check(mn.contains("getRawInputStream"), "has method getRawInputStream");
        check(mn.contains("getSize"), "has method getSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
