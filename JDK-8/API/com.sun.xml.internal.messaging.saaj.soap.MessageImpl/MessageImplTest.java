/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.MessageImpl conformance: abstract class hierarchy and members.
 * @run main MessageImplTest
 */

public class MessageImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.MessageImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.MessageImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("javax.xml.soap.SOAPMessage".equals(c.getSuperclass().getName()), "extends javax.xml.soap.SOAPMessage");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.SOAPConstants"), "has interface javax.xml.soap.SOAPConstants");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("acceptFastInfoset"), "has method acceptFastInfoset");
        check(mn.contains("addAttachmentPart"), "has method addAttachmentPart");
        check(mn.contains("contentType"), "has method contentType");
        check(mn.contains("convertToSingleLine"), "has method convertToSingleLine");
        check(mn.contains("countAttachments"), "has method countAttachments");
        check(mn.contains("createAttachmentPart"), "has method createAttachmentPart");
        check(mn.contains("getAction"), "has method getAction");
        check(mn.contains("getAttachment"), "has method getAttachment");
        check(mn.contains("getAttachmentPart"), "has method getAttachmentPart");
        check(mn.contains("getAttachments"), "has method getAttachments");
        check(mn.contains("getBaseType"), "has method getBaseType");
        check(mn.contains("getCharset"), "has method getCharset");
        check(mn.contains("getCharsetString"), "has method getCharsetString");
        check(mn.contains("getContentDescription"), "has method getContentDescription");
        check(mn.contains("getContentType"), "has method getContentType");
        check(mn.contains("getExpectedAcceptHeader"), "has method getExpectedAcceptHeader");
        check(mn.contains("getExpectedContentType"), "has method getExpectedContentType");
        check(mn.contains("getHeaderBytes"), "has method getHeaderBytes");
        check(mn.contains("getMimeHeaders"), "has method getMimeHeaders");
        check(mn.contains("getMimeMessage"), "has method getMimeMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
