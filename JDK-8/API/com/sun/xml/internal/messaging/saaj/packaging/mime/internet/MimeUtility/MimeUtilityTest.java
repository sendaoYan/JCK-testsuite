/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility conformance: class hierarchy and members.
 * @run main MimeUtilityTest
 */

public class MimeUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("checkAscii"), "has method checkAscii");
        check(mn.contains("decode"), "has method decode");
        check(mn.contains("decodeInnerWords"), "has method decodeInnerWords");
        check(mn.contains("decodeText"), "has method decodeText");
        check(mn.contains("decodeWord"), "has method decodeWord");
        check(mn.contains("doEncode"), "has method doEncode");
        check(mn.contains("encode"), "has method encode");
        check(mn.contains("encodeText"), "has method encodeText");
        check(mn.contains("encodeWord"), "has method encodeWord");
        check(mn.contains("fold"), "has method fold");
        check(mn.contains("getDefaultJavaCharset"), "has method getDefaultJavaCharset");
        check(mn.contains("getDefaultMIMECharset"), "has method getDefaultMIMECharset");
        check(mn.contains("getEncoding"), "has method getEncoding");
        check(mn.contains("indexOfAny"), "has method indexOfAny");
        check(mn.contains("javaCharset"), "has method javaCharset");
        check(mn.contains("loadMappings"), "has method loadMappings");
        check(mn.contains("mimeCharset"), "has method mimeCharset");
        check(mn.contains("nonascii"), "has method nonascii");
        check(mn.contains("quote"), "has method quote");
        check(mn.contains("unfold"), "has method unfold");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
