/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput conformance: class hierarchy and members.
 * @run main XMLSignatureInputTest
 */
public class XMLSignatureInputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput", false, XMLSignatureInputTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isNeedsToBeExpanded"), "method isNeedsToBeExpanded");
        check(mn.contains("setNeedsToBeExpanded"), "method setNeedsToBeExpanded");
        check(mn.contains("getNodeSet"), "method getNodeSet");
        check(mn.contains("getInputNodeSet"), "method getInputNodeSet");
        check(mn.contains("getOctetStream"), "method getOctetStream");
        check(mn.contains("getOctetStreamReal"), "method getOctetStreamReal");
        check(mn.contains("getBytes"), "method getBytes");
        check(mn.contains("isNodeSet"), "method isNodeSet");
        check(mn.contains("isElement"), "method isElement");
        check(mn.contains("isOctetStream"), "method isOctetStream");
        check(mn.contains("isOutputStreamSet"), "method isOutputStreamSet");
        check(mn.contains("isByteArray"), "method isByteArray");
        check(mn.contains("isPreCalculatedDigest"), "method isPreCalculatedDigest");
        check(mn.contains("isInitialized"), "method isInitialized");
        check(mn.contains("getMIMEType"), "method getMIMEType");
        check(mn.contains("setMIMEType"), "method setMIMEType");
        check(mn.contains("getSourceURI"), "method getSourceURI");
        check(mn.contains("setSourceURI"), "method setSourceURI");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("getHTMLRepresentation"), "method getHTMLRepresentation");
        check(mn.contains("getExcludeNode"), "method getExcludeNode");
        check(mn.contains("setExcludeNode"), "method setExcludeNode");
        check(mn.contains("getSubNode"), "method getSubNode");
        check(mn.contains("isExcludeComments"), "method isExcludeComments");
        check(mn.contains("setExcludeComments"), "method setExcludeComments");
        check(mn.contains("updateOutputStream"), "method updateOutputStream");
        check(mn.contains("setOutputStream"), "method setOutputStream");
        check(mn.contains("addNodeFilter"), "method addNodeFilter");
        check(mn.contains("getNodeFilters"), "method getNodeFilters");
        check(mn.contains("setNodeSet"), "method setNodeSet");
        check(mn.contains("convertToNodes"), "method convertToNodes");
        check(mn.contains("isSecureValidation"), "method isSecureValidation");
        check(mn.contains("setSecureValidation"), "method setSecureValidation");
        check(mn.contains("getPreCalculatedDigest"), "method getPreCalculatedDigest");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
