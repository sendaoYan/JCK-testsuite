/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.BMMimeMultipart conformance: class hierarchy and members.
 * @run main BMMimeMultipartTest
 */

public class BMMimeMultipartTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.BMMimeMultipart", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.BMMimeMultipart"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("compile"), "has method compile");
        check(mn.contains("find"), "has method find");
        check(mn.contains("getBoundary"), "has method getBoundary");
        check(mn.contains("getInputStream"), "has method getInputStream");
        check(mn.contains("getNextPart"), "has method getNextPart");
        check(mn.contains("initStream"), "has method initStream");
        check(mn.contains("isEndOfStream"), "has method isEndOfStream");
        check(mn.contains("lastBodyPartFound"), "has method lastBodyPartFound");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("readBody"), "has method readBody");
        check(mn.contains("readHeaders"), "has method readHeaders");
        check(mn.contains("readNext"), "has method readNext");
        check(mn.contains("setBoundary"), "has method setBoundary");
        check(mn.contains("setInputStream"), "has method setInputStream");
        check(mn.contains("setLazyAttachments"), "has method setLazyAttachments");
        check(mn.contains("skipLWSPAndCRLF"), "has method skipLWSPAndCRLF");
        check(mn.contains("skipPreamble"), "has method skipPreamble");
        check(mn.contains("writeTo"), "has method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
