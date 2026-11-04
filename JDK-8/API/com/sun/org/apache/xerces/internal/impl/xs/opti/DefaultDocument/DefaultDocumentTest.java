/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultDocument conformance: class hierarchy and members.
 * @run main DefaultDocumentTest
 */
public class DefaultDocumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultDocument", false, DefaultDocumentTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl"), "superclass com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl");
        check(hasInterface(c, "org.w3c.dom.Document"), "has interface org.w3c.dom.Document");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDoctype"), "method getDoctype");
        check(mn.contains("getImplementation"), "method getImplementation");
        check(mn.contains("getDocumentElement"), "method getDocumentElement");
        check(mn.contains("getElementsByTagName"), "method getElementsByTagName");
        check(mn.contains("getElementsByTagNameNS"), "method getElementsByTagNameNS");
        check(mn.contains("getElementById"), "method getElementById");
        check(mn.contains("importNode"), "method importNode");
        check(mn.contains("createElement"), "method createElement");
        check(mn.contains("createDocumentFragment"), "method createDocumentFragment");
        check(mn.contains("createTextNode"), "method createTextNode");
        check(mn.contains("createComment"), "method createComment");
        check(mn.contains("createCDATASection"), "method createCDATASection");
        check(mn.contains("createProcessingInstruction"), "method createProcessingInstruction");
        check(mn.contains("createAttribute"), "method createAttribute");
        check(mn.contains("createEntityReference"), "method createEntityReference");
        check(mn.contains("createElementNS"), "method createElementNS");
        check(mn.contains("createAttributeNS"), "method createAttributeNS");
        check(mn.contains("getInputEncoding"), "method getInputEncoding");
        check(mn.contains("getXmlEncoding"), "method getXmlEncoding");
        check(mn.contains("getXmlStandalone"), "method getXmlStandalone");
        check(mn.contains("setXmlStandalone"), "method setXmlStandalone");
        check(mn.contains("getXmlVersion"), "method getXmlVersion");
        check(mn.contains("setXmlVersion"), "method setXmlVersion");
        check(mn.contains("getStrictErrorChecking"), "method getStrictErrorChecking");
        check(mn.contains("setStrictErrorChecking"), "method setStrictErrorChecking");
        check(mn.contains("getDocumentURI"), "method getDocumentURI");
        check(mn.contains("setDocumentURI"), "method setDocumentURI");
        check(mn.contains("adoptNode"), "method adoptNode");
        check(mn.contains("normalizeDocument"), "method normalizeDocument");
        check(mn.contains("getDomConfig"), "method getDomConfig");
        check(mn.contains("renameNode"), "method renameNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
