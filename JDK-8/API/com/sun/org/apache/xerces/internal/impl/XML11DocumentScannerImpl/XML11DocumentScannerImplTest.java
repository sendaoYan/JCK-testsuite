/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XML11DocumentScannerImpl conformance: class hierarchy and members.
 * @run main XML11DocumentScannerImplTest
 */
public class XML11DocumentScannerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XML11DocumentScannerImpl", false, XML11DocumentScannerImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl"), "superclass com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("scanContent"), "method scanContent");
        check(mn.contains("scanAttributeValue"), "method scanAttributeValue");
        check(mn.contains("scanPubidLiteral"), "method scanPubidLiteral");
        check(mn.contains("normalizeWhitespace"), "method normalizeWhitespace");
        check(mn.contains("isUnchangedByNormalization"), "method isUnchangedByNormalization");
        check(mn.contains("isInvalid"), "method isInvalid");
        check(mn.contains("isInvalidLiteral"), "method isInvalidLiteral");
        check(mn.contains("isValidNameChar"), "method isValidNameChar");
        check(mn.contains("isValidNameStartChar"), "method isValidNameStartChar");
        check(mn.contains("isValidNCName"), "method isValidNCName");
        check(mn.contains("isValidNameStartHighSurrogate"), "method isValidNameStartHighSurrogate");
        check(mn.contains("versionSupported"), "method versionSupported");
        check(mn.contains("getVersionNotSupportedKey"), "method getVersionNotSupportedKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
