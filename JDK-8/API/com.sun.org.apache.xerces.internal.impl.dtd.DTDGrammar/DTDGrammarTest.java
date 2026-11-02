/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar conformance: class hierarchy and members.
 * @run main DTDGrammarTest
 */
public class DTDGrammarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar", false, DTDGrammarTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDTDHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler"), "has interface com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.validation.EntityState"), "has interface com.sun.org.apache.xerces.internal.impl.validation.EntityState");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.grammars.Grammar"), "has interface com.sun.org.apache.xerces.internal.xni.grammars.Grammar");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getGrammarDescription"), "method getGrammarDescription");
        check(mn.contains("getElementDeclIsExternal"), "method getElementDeclIsExternal");
        check(mn.contains("getAttributeDeclIsExternal"), "method getAttributeDeclIsExternal");
        check(mn.contains("getAttributeDeclIndex"), "method getAttributeDeclIndex");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("startParameterEntity"), "method startParameterEntity");
        check(mn.contains("startExternalSubset"), "method startExternalSubset");
        check(mn.contains("endParameterEntity"), "method endParameterEntity");
        check(mn.contains("endExternalSubset"), "method endExternalSubset");
        check(mn.contains("elementDecl"), "method elementDecl");
        check(mn.contains("attributeDecl"), "method attributeDecl");
        check(mn.contains("internalEntityDecl"), "method internalEntityDecl");
        check(mn.contains("externalEntityDecl"), "method externalEntityDecl");
        check(mn.contains("unparsedEntityDecl"), "method unparsedEntityDecl");
        check(mn.contains("notationDecl"), "method notationDecl");
        check(mn.contains("endDTD"), "method endDTD");
        check(mn.contains("setDTDSource"), "method setDTDSource");
        check(mn.contains("getDTDSource"), "method getDTDSource");
        check(mn.contains("textDecl"), "method textDecl");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startAttlist"), "method startAttlist");
        check(mn.contains("endAttlist"), "method endAttlist");
        check(mn.contains("startConditional"), "method startConditional");
        check(mn.contains("ignoredCharacters"), "method ignoredCharacters");
        check(mn.contains("endConditional"), "method endConditional");
        check(mn.contains("setDTDContentModelSource"), "method setDTDContentModelSource");
        check(mn.contains("getDTDContentModelSource"), "method getDTDContentModelSource");
        check(mn.contains("startContentModel"), "method startContentModel");
        check(mn.contains("startGroup"), "method startGroup");
        check(mn.contains("pcdata"), "method pcdata");
        check(mn.contains("element"), "method element");
        check(mn.contains("separator"), "method separator");
        check(mn.contains("occurrence"), "method occurrence");
        check(mn.contains("endGroup"), "method endGroup");
        check(mn.contains("any"), "method any");
        check(mn.contains("empty"), "method empty");
        check(mn.contains("endContentModel"), "method endContentModel");
        check(mn.contains("isNamespaceAware"), "method isNamespaceAware");
        check(mn.contains("getSymbolTable"), "method getSymbolTable");
        check(mn.contains("getFirstElementDeclIndex"), "method getFirstElementDeclIndex");
        check(mn.contains("getNextElementDeclIndex"), "method getNextElementDeclIndex");
        check(mn.contains("getElementDeclIndex"), "method getElementDeclIndex");
        check(mn.contains("getContentSpecType"), "method getContentSpecType");
        check(mn.contains("getElementDecl"), "method getElementDecl");
        check(mn.contains("getElementDeclName"), "method getElementDeclName");
        check(mn.contains("getFirstAttributeDeclIndex"), "method getFirstAttributeDeclIndex");
        check(mn.contains("getNextAttributeDeclIndex"), "method getNextAttributeDeclIndex");
        check(mn.contains("getAttributeDecl"), "method getAttributeDecl");
        check(mn.contains("isCDATAAttribute"), "method isCDATAAttribute");
        check(mn.contains("getEntityDeclIndex"), "method getEntityDeclIndex");
        check(mn.contains("getEntityDecl"), "method getEntityDecl");
        check(mn.contains("getNotationDeclIndex"), "method getNotationDeclIndex");
        check(mn.contains("getNotationDecl"), "method getNotationDecl");
        check(mn.contains("getContentSpec"), "method getContentSpec");
        check(mn.contains("getContentSpecIndex"), "method getContentSpecIndex");
        check(mn.contains("getContentSpecAsString"), "method getContentSpecAsString");
        check(mn.contains("printElements"), "method printElements");
        check(mn.contains("printAttributes"), "method printAttributes");
        check(mn.contains("addContentSpecToElement"), "method addContentSpecToElement");
        check(mn.contains("getElementContentModelValidator"), "method getElementContentModelValidator");
        check(mn.contains("createElementDecl"), "method createElementDecl");
        check(mn.contains("setElementDecl"), "method setElementDecl");
        check(mn.contains("putElementNameMapping"), "method putElementNameMapping");
        check(mn.contains("setFirstAttributeDeclIndex"), "method setFirstAttributeDeclIndex");
        check(mn.contains("setContentSpecIndex"), "method setContentSpecIndex");
        check(mn.contains("createAttributeDecl"), "method createAttributeDecl");
        check(mn.contains("setAttributeDecl"), "method setAttributeDecl");
        check(mn.contains("createContentSpec"), "method createContentSpec");
        check(mn.contains("setContentSpec"), "method setContentSpec");
        check(mn.contains("createEntityDecl"), "method createEntityDecl");
        check(mn.contains("setEntityDecl"), "method setEntityDecl");
        check(mn.contains("createNotationDecl"), "method createNotationDecl");
        check(mn.contains("setNotationDecl"), "method setNotationDecl");
        check(mn.contains("addContentSpecNode"), "method addContentSpecNode");
        check(mn.contains("addUniqueLeafNode"), "method addUniqueLeafNode");
        check(mn.contains("initializeContentModelStack"), "method initializeContentModelStack");
        check(mn.contains("isImmutable"), "method isImmutable");
        check(mn.contains("isEntityDeclared"), "method isEntityDeclared");
        check(mn.contains("isEntityUnparsed"), "method isEntityUnparsed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
