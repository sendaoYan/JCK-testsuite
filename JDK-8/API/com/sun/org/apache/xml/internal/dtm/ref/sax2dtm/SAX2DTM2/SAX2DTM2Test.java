/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2 conformance: class hierarchy and members.
 * @run main SAX2DTM2Test
 */
public class SAX2DTM2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2", false, SAX2DTM2Test.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM"), "superclass com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_exptype"), "method _exptype");
        check(mn.contains("_exptype2"), "method _exptype2");
        check(mn.contains("_nextsib2"), "method _nextsib2");
        check(mn.contains("_firstch2"), "method _firstch2");
        check(mn.contains("_parent2"), "method _parent2");
        check(mn.contains("_type2"), "method _type2");
        check(mn.contains("getExpandedTypeID2"), "method getExpandedTypeID2");
        check(mn.contains("_exptype2Type"), "method _exptype2Type");
        check(mn.contains("getIdForNamespace"), "method getIdForNamespace");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("addNode"), "method addNode");
        check(mn.contains("charactersFlush"), "method charactersFlush");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("getFirstAttribute"), "method getFirstAttribute");
        check(mn.contains("getFirstAttributeIdentity"), "method getFirstAttributeIdentity");
        check(mn.contains("getNextAttributeIdentity"), "method getNextAttributeIdentity");
        check(mn.contains("getTypedAttribute"), "method getTypedAttribute");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getNodeNameX"), "method getNodeNameX");
        check(mn.contains("getNodeName"), "method getNodeName");
        check(mn.contains("getStringValue"), "method getStringValue");
        check(mn.contains("getStringValueX"), "method getStringValueX");
        check(mn.contains("dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("copyTextNode"), "method copyTextNode");
        check(mn.contains("copyElement"), "method copyElement");
        check(mn.contains("copyNS"), "method copyNS");
        check(mn.contains("getNextNamespaceNode2"), "method getNextNamespaceNode2");
        check(mn.contains("copyAttributes"), "method copyAttributes");
        check(mn.contains("copyAttribute"), "method copyAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
