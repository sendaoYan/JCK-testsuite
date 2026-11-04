/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLEntityScanner conformance: class hierarchy and members.
 * @run main XMLEntityScannerTest
 */
public class XMLEntityScannerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLEntityScanner", false, XMLEntityScannerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLLocator"), "has interface com.sun.org.apache.xerces.internal.xni.XMLLocator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setBufferSize"), "method setBufferSize");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getXMLVersion"), "method getXMLVersion");
        check(mn.contains("setXMLVersion"), "method setXMLVersion");
        check(mn.contains("setCurrentEntity"), "method setCurrentEntity");
        check(mn.contains("getCurrentEntity"), "method getCurrentEntity");
        check(mn.contains("getBaseSystemId"), "method getBaseSystemId");
        check(mn.contains("setBaseSystemId"), "method setBaseSystemId");
        check(mn.contains("getLineNumber"), "method getLineNumber");
        check(mn.contains("setLineNumber"), "method setLineNumber");
        check(mn.contains("getColumnNumber"), "method getColumnNumber");
        check(mn.contains("setColumnNumber"), "method setColumnNumber");
        check(mn.contains("getCharacterOffset"), "method getCharacterOffset");
        check(mn.contains("getExpandedSystemId"), "method getExpandedSystemId");
        check(mn.contains("setExpandedSystemId"), "method setExpandedSystemId");
        check(mn.contains("getLiteralSystemId"), "method getLiteralSystemId");
        check(mn.contains("setLiteralSystemId"), "method setLiteralSystemId");
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("setPublicId"), "method setPublicId");
        check(mn.contains("setVersion"), "method setVersion");
        check(mn.contains("getVersion"), "method getVersion");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("setEncoding"), "method setEncoding");
        check(mn.contains("isExternal"), "method isExternal");
        check(mn.contains("getChar"), "method getChar");
        check(mn.contains("peekChar"), "method peekChar");
        check(mn.contains("scanChar"), "method scanChar");
        check(mn.contains("scanNmtoken"), "method scanNmtoken");
        check(mn.contains("scanName"), "method scanName");
        check(mn.contains("scanQName"), "method scanQName");
        check(mn.contains("checkBeforeLoad"), "method checkBeforeLoad");
        check(mn.contains("checkEntityLimit"), "method checkEntityLimit");
        check(mn.contains("checkNodeCount"), "method checkNodeCount");
        check(mn.contains("checkLimit"), "method checkLimit");
        check(mn.contains("scanContent"), "method scanContent");
        check(mn.contains("scanLiteral"), "method scanLiteral");
        check(mn.contains("storeWhiteSpace"), "method storeWhiteSpace");
        check(mn.contains("scanData"), "method scanData");
        check(mn.contains("skipChar"), "method skipChar");
        check(mn.contains("isSpace"), "method isSpace");
        check(mn.contains("skipSpaces"), "method skipSpaces");
        check(mn.contains("arrangeCapacity"), "method arrangeCapacity");
        check(mn.contains("skipString"), "method skipString");
        check(mn.contains("load"), "method load");
        check(mn.contains("createReader"), "method createReader");
        check(mn.contains("getEncodingName"), "method getEncodingName");
        check(mn.contains("print"), "method print");
        check(mn.contains("registerListener"), "method registerListener");
        check(mn.contains("invokeListeners"), "method invokeListeners");
        check(mn.contains("skipDeclSpaces"), "method skipDeclSpaces");
        check(mn.contains("normalizeNewlines"), "method normalizeNewlines");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
