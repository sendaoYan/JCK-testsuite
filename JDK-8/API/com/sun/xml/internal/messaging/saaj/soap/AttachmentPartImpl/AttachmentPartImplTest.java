/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.AttachmentPartImpl conformance: class hierarchy and members.
 * @run main AttachmentPartImplTest
 */

public class AttachmentPartImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.AttachmentPartImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.AttachmentPartImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.soap.AttachmentPart".equals(c.getSuperclass().getName()), "extends javax.xml.soap.AttachmentPart");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addMimeHeader"), "has method addMimeHeader");
        check(mn.contains("clearContent"), "has method clearContent");
        check(mn.contains("cmdMapInitialized"), "has method cmdMapInitialized");
        check(mn.contains("copyMimeHeaders"), "has method copyMimeHeaders");
        check(mn.contains("equals"), "has method equals");
        check(mn.contains("getAllMimeHeaders"), "has method getAllMimeHeaders");
        check(mn.contains("getBase64Content"), "has method getBase64Content");
        check(mn.contains("getContent"), "has method getContent");
        check(mn.contains("getDataHandler"), "has method getDataHandler");
        check(mn.contains("getMatchingMimeHeaders"), "has method getMatchingMimeHeaders");
        check(mn.contains("getMimeHeader"), "has method getMimeHeader");
        check(mn.contains("getMimeHeaders"), "has method getMimeHeaders");
        check(mn.contains("getMimePart"), "has method getMimePart");
        check(mn.contains("getNonMatchingMimeHeaders"), "has method getNonMatchingMimeHeaders");
        check(mn.contains("getRawContent"), "has method getRawContent");
        check(mn.contains("getRawContentBytes"), "has method getRawContentBytes");
        check(mn.contains("getSize"), "has method getSize");
        check(mn.contains("hasAllHeaders"), "has method hasAllHeaders");
        check(mn.contains("hashCode"), "has method hashCode");
        check(mn.contains("initializeJavaActivationHandlers"), "has method initializeJavaActivationHandlers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
