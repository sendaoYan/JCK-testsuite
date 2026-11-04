/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.XPathContext conformance: reflection.
 * @run main XPathContextTest
 */

public class XPathContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.XPathContext", false, XPathContextTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPathContext"), "simple name XPathContext");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.DTMManager"), "superclass com.sun.org.apache.xml.internal.dtm.DTMManager");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getDTMManager"), "method getDTMManager");
        check(has(c, "setSecureProcessing"), "method setSecureProcessing");
        check(has(c, "isSecureProcessing"), "method isSecureProcessing");
        check(has(c, "getDTM"), "method getDTM");
        check(has(c, "getDTMHandleFromNode"), "method getDTMHandleFromNode");
        check(has(c, "getDTMIdentity"), "method getDTMIdentity");
        check(has(c, "createDocumentFragment"), "method createDocumentFragment");
        check(has(c, "release"), "method release");
        check(has(c, "createDTMIterator"), "method createDTMIterator");
        check(has(c, "reset"), "method reset");
        check(has(c, "setSAXLocator"), "method setSAXLocator");
        check(has(c, "pushSAXLocator"), "method pushSAXLocator");
        check(has(c, "pushSAXLocatorNull"), "method pushSAXLocatorNull");
        check(has(c, "popSAXLocator"), "method popSAXLocator");
        check(has(c, "getSAXLocator"), "method getSAXLocator");
        check(has(c, "getOwnerObject"), "method getOwnerObject");
        check(has(c, "getVarStack"), "method getVarStack");
        check(has(c, "setVarStack"), "method setVarStack");
        check(has(c, "getSourceTreeManager"), "method getSourceTreeManager");
        check(has(c, "setSourceTreeManager"), "method setSourceTreeManager");
        check(has(c, "getErrorListener"), "method getErrorListener");
        check(has(c, "setErrorListener"), "method setErrorListener");
        check(has(c, "getURIResolver"), "method getURIResolver");
        check(has(c, "setURIResolver"), "method setURIResolver");
        check(has(c, "getPrimaryReader"), "method getPrimaryReader");
        check(has(c, "setPrimaryReader"), "method setPrimaryReader");
        check(has(c, "getContextNodeListsStack"), "method getContextNodeListsStack");
        check(has(c, "setContextNodeListsStack"), "method setContextNodeListsStack");
        check(has(c, "getContextNodeList"), "method getContextNodeList");
        check(has(c, "pushContextNodeList"), "method pushContextNodeList");
        check(has(c, "popContextNodeList"), "method popContextNodeList");
        check(has(c, "getCurrentNodeStack"), "method getCurrentNodeStack");
        check(has(c, "setCurrentNodeStack"), "method setCurrentNodeStack");
        check(has(c, "getCurrentNode"), "method getCurrentNode");
        check(has(c, "pushCurrentNodeAndExpression"), "method pushCurrentNodeAndExpression");
        check(has(c, "popCurrentNodeAndExpression"), "method popCurrentNodeAndExpression");
        check(has(c, "pushExpressionState"), "method pushExpressionState");
        check(has(c, "popExpressionState"), "method popExpressionState");
        check(has(c, "pushCurrentNode"), "method pushCurrentNode");
        check(has(c, "popCurrentNode"), "method popCurrentNode");
        check(has(c, "pushPredicateRoot"), "method pushPredicateRoot");
        check(has(c, "popPredicateRoot"), "method popPredicateRoot");
        check(has(c, "getPredicateRoot"), "method getPredicateRoot");
        check(has(c, "pushIteratorRoot"), "method pushIteratorRoot");
        check(has(c, "popIteratorRoot"), "method popIteratorRoot");
        check(has(c, "getIteratorRoot"), "method getIteratorRoot");
        check(has(c, "getCurrentExpressionNodeStack"), "method getCurrentExpressionNodeStack");
        check(has(c, "setCurrentExpressionNodeStack"), "method setCurrentExpressionNodeStack");
        check(has(c, "getPredicatePos"), "method getPredicatePos");
        check(has(c, "pushPredicatePos"), "method pushPredicatePos");
        check(has(c, "popPredicatePos"), "method popPredicatePos");
        check(has(c, "getCurrentExpressionNode"), "method getCurrentExpressionNode");
        check(has(c, "pushCurrentExpressionNode"), "method pushCurrentExpressionNode");
        check(has(c, "popCurrentExpressionNode"), "method popCurrentExpressionNode");
        check(has(c, "getNamespaceContext"), "method getNamespaceContext");
        check(has(c, "setNamespaceContext"), "method setNamespaceContext");
        check(has(c, "pushNamespaceContext"), "method pushNamespaceContext");
        check(has(c, "pushNamespaceContextNull"), "method pushNamespaceContextNull");
        check(has(c, "popNamespaceContext"), "method popNamespaceContext");
        check(has(c, "getAxesIteratorStackStacks"), "method getAxesIteratorStackStacks");
        check(has(c, "setAxesIteratorStackStacks"), "method setAxesIteratorStackStacks");
        check(has(c, "pushSubContextList"), "method pushSubContextList");
        check(has(c, "popSubContextList"), "method popSubContextList");
        check(has(c, "getSubContextList"), "method getSubContextList");
        check(has(c, "getCurrentNodeList"), "method getCurrentNodeList");
        check(has(c, "getContextNode"), "method getContextNode");
        check(has(c, "getContextNodes"), "method getContextNodes");
        check(has(c, "getExpressionContext"), "method getExpressionContext");
        check(has(c, "getGlobalRTFDTM"), "method getGlobalRTFDTM");
        check(has(c, "getRTFDTM"), "method getRTFDTM");
        check(has(c, "pushRTFContext"), "method pushRTFContext");
        check(has(c, "popRTFContext"), "method popRTFContext");
        check(has(c, "getDTMXRTreeFrag"), "method getDTMXRTreeFrag");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
