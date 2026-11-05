/*
 * @test
 * @summary org.w3c.dom.ProcessingInstruction conformance: interface extends Node.
 * @run main ProcessingInstructionTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class ProcessingInstructionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(ProcessingInstruction.class.isInterface(),
              "ProcessingInstruction is an interface");
        check(Node.class.isAssignableFrom(ProcessingInstruction.class), "extends Node");
        check(ProcessingInstruction.class.getMethod("getTarget") != null, "getTarget()");
        check(ProcessingInstruction.class.getMethod("getData") != null, "getData()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ProcessingInstruction a = new org.w3c.dom.ProcessingInstruction(){
            public java.lang.String getTarget() { return "s"; }
            public java.lang.String getData() { return "s"; }
            public void setData(java.lang.String p0) { }
            public java.lang.String getNodeName() { return "s"; }
            public java.lang.String getNodeValue() { return "s"; }
            public void setNodeValue(java.lang.String p0) { }
            public short getNodeType() { return (short)2; }
            public org.w3c.dom.Node getParentNode() { return null; }
            public org.w3c.dom.NodeList getChildNodes() { return null; }
            public org.w3c.dom.Node getFirstChild() { return null; }
            public org.w3c.dom.Node getLastChild() { return null; }
            public org.w3c.dom.Node getPreviousSibling() { return null; }
            public org.w3c.dom.Node getNextSibling() { return null; }
            public org.w3c.dom.NamedNodeMap getAttributes() { return null; }
            public org.w3c.dom.Document getOwnerDocument() { return null; }
            public org.w3c.dom.Node insertBefore(org.w3c.dom.Node p0, org.w3c.dom.Node p1) { return null; }
            public org.w3c.dom.Node replaceChild(org.w3c.dom.Node p0, org.w3c.dom.Node p1) { return null; }
            public org.w3c.dom.Node removeChild(org.w3c.dom.Node p0) { return null; }
            public org.w3c.dom.Node appendChild(org.w3c.dom.Node p0) { return null; }
            public boolean hasChildNodes() { return true; }
            public org.w3c.dom.Node cloneNode(boolean p0) { return null; }
            public void normalize() { }
            public boolean isSupported(java.lang.String p0, java.lang.String p1) { return true; }
            public java.lang.String getNamespaceURI() { return "s"; }
            public java.lang.String getPrefix() { return "s"; }
            public void setPrefix(java.lang.String p0) { }
            public java.lang.String getLocalName() { return "s"; }
            public boolean hasAttributes() { return true; }
            public java.lang.String getBaseURI() { return "s"; }
            public short compareDocumentPosition(org.w3c.dom.Node p0) { return (short)2; }
            public java.lang.String getTextContent() { return "s"; }
            public void setTextContent(java.lang.String p0) { }
            public boolean isSameNode(org.w3c.dom.Node p0) { return true; }
            public java.lang.String lookupPrefix(java.lang.String p0) { return "s"; }
            public boolean isDefaultNamespace(java.lang.String p0) { return true; }
            public java.lang.String lookupNamespaceURI(java.lang.String p0) { return "s"; }
            public boolean isEqualNode(org.w3c.dom.Node p0) { return true; }
            public java.lang.Object getFeature(java.lang.String p0, java.lang.String p1) { return null; }
            public java.lang.Object setUserData(java.lang.String p0, java.lang.Object p1, org.w3c.dom.UserDataHandler p2) { return null; }
            public java.lang.Object getUserData(java.lang.String p0) { return null; }
        };
        check(a != null, "anon impl of ProcessingInstruction");
        check("s".equals(a.getTarget()), "getTarget() returns fixed default");
        check("s".equals(a.getData()), "getData() returns fixed default");
        a.setData("s");
        check("s".equals(a.getNodeName()), "getNodeName() returns fixed default");
        check("s".equals(a.getNodeValue()), "getNodeValue() returns fixed default");
        a.setNodeValue("s");
        check(a.getNodeType() == (short)2, "getNodeType() returns fixed default");
        check(a.getParentNode() == null, "getParentNode() returns fixed default");
        check(a.getChildNodes() == null, "getChildNodes() returns fixed default");
        check(a.getFirstChild() == null, "getFirstChild() returns fixed default");
        check(a.getLastChild() == null, "getLastChild() returns fixed default");
        check(a.getPreviousSibling() == null, "getPreviousSibling() returns fixed default");
        check(a.getNextSibling() == null, "getNextSibling() returns fixed default");
        check(a.getAttributes() == null, "getAttributes() returns fixed default");
        check(a.getOwnerDocument() == null, "getOwnerDocument() returns fixed default");
        check(a.insertBefore((org.w3c.dom.Node) null, (org.w3c.dom.Node) null) == null, "insertBefore() returns fixed default");
        check(a.replaceChild((org.w3c.dom.Node) null, (org.w3c.dom.Node) null) == null, "replaceChild() returns fixed default");
        check(a.removeChild((org.w3c.dom.Node) null) == null, "removeChild() returns fixed default");
        check(a.appendChild((org.w3c.dom.Node) null) == null, "appendChild() returns fixed default");
        check(a.hasChildNodes() == true, "hasChildNodes() returns fixed default");
        check(a.cloneNode(true) == null, "cloneNode() returns fixed default");
        a.normalize();
        check(a.isSupported("s", "s") == true, "isSupported() returns fixed default");
        check("s".equals(a.getNamespaceURI()), "getNamespaceURI() returns fixed default");
        check("s".equals(a.getPrefix()), "getPrefix() returns fixed default");
        a.setPrefix("s");
        check("s".equals(a.getLocalName()), "getLocalName() returns fixed default");
        check(a.hasAttributes() == true, "hasAttributes() returns fixed default");
        check("s".equals(a.getBaseURI()), "getBaseURI() returns fixed default");
        check(a.compareDocumentPosition((org.w3c.dom.Node) null) == (short)2, "compareDocumentPosition() returns fixed default");
        check("s".equals(a.getTextContent()), "getTextContent() returns fixed default");
        a.setTextContent("s");
        check(a.isSameNode((org.w3c.dom.Node) null) == true, "isSameNode() returns fixed default");
        check("s".equals(a.lookupPrefix("s")), "lookupPrefix() returns fixed default");
        check(a.isDefaultNamespace("s") == true, "isDefaultNamespace() returns fixed default");
        check("s".equals(a.lookupNamespaceURI("s")), "lookupNamespaceURI() returns fixed default");
        check(a.isEqualNode((org.w3c.dom.Node) null) == true, "isEqualNode() returns fixed default");
        check(a.getFeature("s", "s") == null, "getFeature() returns fixed default");
        check(a.setUserData("s", (java.lang.Object) null, (org.w3c.dom.UserDataHandler) null) == null, "setUserData() returns fixed default");
        check(a.getUserData("s") == null, "getUserData() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral ProcessingInstruction contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ProcessingInstruction pi = d.createProcessingInstruction("app", "instr");
        check("app".equals(pi.getTarget()), "PI target");
        check("instr".equals(pi.getData()) && "instr".equals(pi.getNodeValue()), "PI data via getData/getNodeValue");
        check(org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE == pi.getNodeType(), "PI nodeType 7");
        check("app".equals(pi.getNodeName()), "PI nodeName is the target");
        check("instr".equals(pi.getTextContent()), "PI textContent is the data");
        // setData syncs node value
        pi.setData("v2");
        check("v2".equals(pi.getNodeValue()) && "v2".equals(pi.getTextContent()), "PI setData syncs nodeValue/textContent");
        pi.setTextContent("v3");
        check("v3".equals(pi.getData()), "PI setTextContent syncs getData");
        pi.setData(null);
        check(pi.getData() == null && pi.getNodeValue() == null, "PI setData(null) -> null data (Xerces)");
        pi.setData("back");
        check("back".equals(pi.getData()), "PI setData recovers");
        check(d.createProcessingInstruction("app", null).getData() == null, "createProcessingInstruction(target, null) -> null data");
        // target legality
        try { d.createProcessingInstruction("a b", "d"); check(false, "PI target with space must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "PI target 'a b' -> INVALID_CHARACTER_ERR"); }
        try { d.createProcessingInstruction("", "d"); check(false, "PI empty target must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "PI target '' -> INVALID_CHARACTER_ERR"); }
        try { d.createProcessingInstruction("a<b", "d"); check(false, "PI target with '<' must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "PI target 'a<b' -> INVALID_CHARACTER_ERR"); }
        check(d.createProcessingInstruction("xml", "d") != null, "PI target 'xml' allowed at API level (both sides)");
        check(d.createProcessingInstruction("xml-stylesheet", "d") != null, "PI target 'xml-stylesheet' allowed");
        // leaf structure
        check(pi.getChildNodes().getLength() == 0 && pi.getFirstChild() == null, "PI has no children");
        check(pi.getAttributes() == null, "PI getAttributes null");
        // clone keeps target and data, edits independent
        org.w3c.dom.ProcessingInstruction cl = (org.w3c.dom.ProcessingInstruction) pi.cloneNode(true);
        check("app".equals(cl.getTarget()) && "back".equals(cl.getData()), "PI clone keeps target+data");
        cl.setData("other");
        check(!cl.getData().equals(pi.getData()), "PI clone data independent");
        // importNode rebinding
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ProcessingInstruction imp = (org.w3c.dom.ProcessingInstruction) other.importNode(pi, true);
        check(imp.getOwnerDocument() == other && "app".equals(imp.getTarget()) && "back".equals(imp.getData()), "PI importNode rebinds owner");
        // document-level placement (before root) is legal
        org.w3c.dom.Document d3 = f.newDocumentBuilder().newDocument();
        d3.appendChild(d3.createProcessingInstruction("top", "x"));
        d3.appendChild(d3.createElement("r"));
        check(org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE == d3.getFirstChild().getNodeType() && d3.getChildNodes().getLength() == 2, "PI allowed at document level");
        // equality
        check(pi.isEqualNode(d.createProcessingInstruction("app", "back")), "PI isEqualNode target+data");
        check(!pi.isEqualNode(d.createProcessingInstruction("app2", "back")), "PI differs on target");
        // parse: target/data split at first whitespace
        String xml = "<r><?xml-stylesheet href='x.css' type='text/css'?></r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.ProcessingInstruction ppi = (org.w3c.dom.ProcessingInstruction) pd.getDocumentElement().getFirstChild();
        check("xml-stylesheet".equals(ppi.getTarget()), "parsed PI target");
        check("href='x.css' type='text/css'".equals(ppi.getData()), "parsed PI data is remainder after target");
    }
}
