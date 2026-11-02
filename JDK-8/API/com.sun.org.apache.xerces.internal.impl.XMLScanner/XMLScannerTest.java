/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLScanner conformance: abstract class hierarchy and members.
 * @run main XMLScannerTest
 */
public class XMLScannerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLScanner", false, XMLScannerTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("reset"), "method reset");
        check(mn.contains("setPropertyManager"), "method setPropertyManager");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("scanXMLDeclOrTextDecl"), "method scanXMLDeclOrTextDecl");
        check(mn.contains("scanPseudoAttribute"), "method scanPseudoAttribute");
        check(mn.contains("scanPI"), "method scanPI");
        check(mn.contains("scanPIData"), "method scanPIData");
        check(mn.contains("scanComment"), "method scanComment");
        check(mn.contains("scanAttributeValue"), "method scanAttributeValue");
        check(mn.contains("resolveCharacter"), "method resolveCharacter");
        check(mn.contains("scanExternalID"), "method scanExternalID");
        check(mn.contains("scanPubidLiteral"), "method scanPubidLiteral");
        check(mn.contains("normalizeWhitespace"), "method normalizeWhitespace");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("scanCharReferenceValue"), "method scanCharReferenceValue");
        check(mn.contains("isInvalid"), "method isInvalid");
        check(mn.contains("isInvalidLiteral"), "method isInvalidLiteral");
        check(mn.contains("isValidNameChar"), "method isValidNameChar");
        check(mn.contains("isValidNCName"), "method isValidNCName");
        check(mn.contains("isValidNameStartChar"), "method isValidNameStartChar");
        check(mn.contains("isValidNameStartHighSurrogate"), "method isValidNameStartHighSurrogate");
        check(mn.contains("versionSupported"), "method versionSupported");
        check(mn.contains("scanSurrogates"), "method scanSurrogates");
        check(mn.contains("reportFatalError"), "method reportFatalError");
        check(mn.contains("getStringBuffer"), "method getStringBuffer");
        check(mn.contains("checkEntityLimit"), "method checkEntityLimit");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
