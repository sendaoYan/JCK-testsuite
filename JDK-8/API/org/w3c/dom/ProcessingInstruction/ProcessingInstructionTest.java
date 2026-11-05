/*
 * @test
 * @summary org.w3c.dom.ProcessingInstruction conformance: reflection shape checks.
 * @run main ProcessingInstructionTest
 */

import java.lang.reflect.*;

public class ProcessingInstructionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.ProcessingInstruction");
        check(c != null, "org.w3c.dom.ProcessingInstruction loads");
        check(c.getName().equals("org.w3c.dom.ProcessingInstruction"), "binary name is org.w3c.dom.ProcessingInstruction");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ProcessingInstruction is public");
        check(c.isInterface(), "org.w3c.dom.ProcessingInstruction is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getTarget"), "declares getTarget");
        check(declares(c, "getData"), "declares getData");
        check(declares(c, "setData"), "declares setData");
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
