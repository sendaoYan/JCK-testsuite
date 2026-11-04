/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.XMLUtils conformance: class hierarchy and members.
 * @run main XMLUtilsTest
 */
public class XMLUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.XMLUtils", false, XMLUtilsTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setDsPrefix"), "method setDsPrefix");
        check(mn.contains("setDs11Prefix"), "method setDs11Prefix");
        check(mn.contains("setXencPrefix"), "method setXencPrefix");
        check(mn.contains("setXenc11Prefix"), "method setXenc11Prefix");
        check(mn.contains("getNextElement"), "method getNextElement");
        check(mn.contains("getSet"), "method getSet");
        check(mn.contains("outputDOM"), "method outputDOM");
        check(mn.contains("outputDOMc14nWithComments"), "method outputDOMc14nWithComments");
        check(mn.contains("getFullTextChildrenFromElement"), "method getFullTextChildrenFromElement");
        check(mn.contains("getFullTextChildrenFromNode"), "method getFullTextChildrenFromNode");
        check(mn.contains("createElementInSignatureSpace"), "method createElementInSignatureSpace");
        check(mn.contains("createElementInSignature11Space"), "method createElementInSignature11Space");
        check(mn.contains("elementIsInSignatureSpace"), "method elementIsInSignatureSpace");
        check(mn.contains("elementIsInSignature11Space"), "method elementIsInSignature11Space");
        check(mn.contains("getOwnerDocument"), "method getOwnerDocument");
        check(mn.contains("createDSctx"), "method createDSctx");
        check(mn.contains("addReturnToElement"), "method addReturnToElement");
        check(mn.contains("addReturnBeforeChild"), "method addReturnBeforeChild");
        check(mn.contains("encodeToString"), "method encodeToString");
        check(mn.contains("decode"), "method decode");
        check(mn.contains("isIgnoreLineBreaks"), "method isIgnoreLineBreaks");
        check(mn.contains("convertNodelistToSet"), "method convertNodelistToSet");
        check(mn.contains("circumventBug2650"), "method circumventBug2650");
        check(mn.contains("selectDsNode"), "method selectDsNode");
        check(mn.contains("selectDs11Node"), "method selectDs11Node");
        check(mn.contains("selectDsNodeText"), "method selectDsNodeText");
        check(mn.contains("selectDs11NodeText"), "method selectDs11NodeText");
        check(mn.contains("selectNodeText"), "method selectNodeText");
        check(mn.contains("selectNode"), "method selectNode");
        check(mn.contains("selectDsNodes"), "method selectDsNodes");
        check(mn.contains("selectDs11Nodes"), "method selectDs11Nodes");
        check(mn.contains("selectNodes"), "method selectNodes");
        check(mn.contains("excludeNodeFromSet"), "method excludeNodeFromSet");
        check(mn.contains("getStrFromNode"), "method getStrFromNode");
        check(mn.contains("isDescendantOrSelf"), "method isDescendantOrSelf");
        check(mn.contains("ignoreLineBreaks"), "method ignoreLineBreaks");
        check(mn.contains("getAttributeValue"), "method getAttributeValue");
        check(mn.contains("protectAgainstWrappingAttack"), "method protectAgainstWrappingAttack");
        check(mn.contains("createDocumentBuilder"), "method createDocumentBuilder");
        check(mn.contains("getBytes"), "method getBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
