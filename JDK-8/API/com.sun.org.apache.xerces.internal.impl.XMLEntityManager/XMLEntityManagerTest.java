/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLEntityManager conformance: class hierarchy and members.
 * @run main XMLEntityManagerTest
 */
public class XMLEntityManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLEntityManager", false, XMLEntityManagerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addInternalEntity"), "method addInternalEntity");
        check(mn.contains("addExternalEntity"), "method addExternalEntity");
        check(mn.contains("addUnparsedEntity"), "method addUnparsedEntity");
        check(mn.contains("getEntityStore"), "method getEntityStore");
        check(mn.contains("getEntityScanner"), "method getEntityScanner");
        check(mn.contains("setScannerVersion"), "method setScannerVersion");
        check(mn.contains("setupCurrentEntity"), "method setupCurrentEntity");
        check(mn.contains("isExternalEntity"), "method isExternalEntity");
        check(mn.contains("isEntityDeclInExternalSubset"), "method isEntityDeclInExternalSubset");
        check(mn.contains("setStandalone"), "method setStandalone");
        check(mn.contains("isStandalone"), "method isStandalone");
        check(mn.contains("isDeclaredEntity"), "method isDeclaredEntity");
        check(mn.contains("isUnparsedEntity"), "method isUnparsedEntity");
        check(mn.contains("getCurrentResourceIdentifier"), "method getCurrentResourceIdentifier");
        check(mn.contains("setEntityHandler"), "method setEntityHandler");
        check(mn.contains("resolveEntityAsPerStax"), "method resolveEntityAsPerStax");
        check(mn.contains("resolveEntity"), "method resolveEntity");
        check(mn.contains("startEntity"), "method startEntity");
        check(mn.contains("startDocumentEntity"), "method startDocumentEntity");
        check(mn.contains("startDTDEntity"), "method startDTDEntity");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("getCurrentEntity"), "method getCurrentEntity");
        check(mn.contains("getTopLevelEntity"), "method getTopLevelEntity");
        check(mn.contains("closeReaders"), "method closeReaders");
        check(mn.contains("endEntity"), "method endEntity");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getRecognizedFeatures"), "method getRecognizedFeatures");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("setLimitAnalyzer"), "method setLimitAnalyzer");
        check(mn.contains("getRecognizedProperties"), "method getRecognizedProperties");
        check(mn.contains("getFeatureDefault"), "method getFeatureDefault");
        check(mn.contains("getPropertyDefault"), "method getPropertyDefault");
        check(mn.contains("expandSystemId"), "method expandSystemId");
        check(mn.contains("createOutputStream"), "method createOutputStream");
        check(mn.contains("absolutizeAgainstUserDir"), "method absolutizeAgainstUserDir");
        check(mn.contains("setInstanceFollowRedirects"), "method setInstanceFollowRedirects");
        check(mn.contains("getEncodingName"), "method getEncodingName");
        check(mn.contains("createReader"), "method createReader");
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getExpandedSystemId"), "method getExpandedSystemId");
        check(mn.contains("getLiteralSystemId"), "method getLiteralSystemId");
        check(mn.contains("getLineNumber"), "method getLineNumber");
        check(mn.contains("getColumnNumber"), "method getColumnNumber");
        check(mn.contains("fixURI"), "method fixURI");
        check(mn.contains("print"), "method print");
        check(mn.contains("test"), "method test");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
