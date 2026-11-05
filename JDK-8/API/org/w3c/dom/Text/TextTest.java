/*
 * @test
 * @summary org.w3c.dom.Text conformance: reflection shape checks.
 * @run main TextTest
 */

import java.lang.reflect.*;

public class TextTest {
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
        Class<?> c = Class.forName("org.w3c.dom.Text");
        check(c != null, "org.w3c.dom.Text loads");
        check(c.getName().equals("org.w3c.dom.Text"), "binary name is org.w3c.dom.Text");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.Text is public");
        check(c.isInterface(), "org.w3c.dom.Text is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.CharacterData")), "direct superinterfaces");
        check(declares(c, "splitText"), "declares splitText");
        check(declares(c, "isElementContentWhitespace"), "declares isElementContentWhitespace");
        check(declares(c, "getWholeText"), "declares getWholeText");
        check(declares(c, "replaceWholeText"), "declares replaceWholeText");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Text contract (splitText/wholeText/normalize) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element e = d.createElement("e");
        d.appendChild(e);
        // splitText mid-node
        org.w3c.dom.Text t = d.createTextNode("hello");
        e.appendChild(t);
        org.w3c.dom.Text tail = t.splitText(2);
        check("he".equals(t.getData()), "splitText keeps prefix in original");
        check("llo".equals(tail.getData()), "splitText moves suffix to new node");
        check(e.getChildNodes().getLength() == 2, "splitText grows parent child count");
        check(tail.getParentNode() == e, "new half attaches to same parent");
        check(tail.getPreviousSibling() == t && t.getNextSibling() == tail, "new half inserted directly after original");
        check(tail.getOwnerDocument() == d, "new half keeps owner document");
        // splitText at both extremes
        org.w3c.dom.Text t2 = d.createTextNode("abc");
        e.appendChild(t2);
        org.w3c.dom.Text emptyTail = t2.splitText(3);
        check("".equals(emptyTail.getData()) && "abc".equals(t2.getData()), "splitText(len) -> empty sibling");
        org.w3c.dom.Text t3 = d.createTextNode("xyz");
        e.appendChild(t3);
        org.w3c.dom.Text fullTail = t3.splitText(0);
        check("".equals(t3.getData()) && "xyz".equals(fullTail.getData()), "splitText(0) empties original, new holds all");
        // splitText bounds
        try { d.createTextNode("ab").splitText(99); check(false, "splitText offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "splitText(99) -> INDEX_SIZE_ERR"); }
        try { d.createTextNode("ab").splitText(-1); check(false, "splitText negative offset must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "splitText(-1) -> INDEX_SIZE_ERR"); }
        // parentless splitText still works
        org.w3c.dom.Text free = d.createTextNode("abcd");
        org.w3c.dom.Text freeTail = free.splitText(1);
        check("a".equals(free.getData()) && "bcd".equals(freeTail.getData()), "parentless splitText succeeds");
        check(freeTail.getParentNode() == null, "parentless split half has no parent");
        // getWholeText spans adjacent Text and CDATA, comments break adjacency
        org.w3c.dom.Document d2 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element e2 = d2.createElement("e2");
        d2.appendChild(e2);
        org.w3c.dom.Text a = d2.createTextNode("a");
        e2.appendChild(a);
        e2.appendChild(d2.createCDATASection("b"));
        e2.appendChild(d2.createTextNode("c"));
        check("abc".equals(a.getWholeText()), "getWholeText spans adjacent Text+CDATA");
        org.w3c.dom.Element e3 = d2.createElement("e3");
        e2.appendChild(e3);
        org.w3c.dom.Text a3 = d2.createTextNode("a");
        e3.appendChild(a3);
        e3.appendChild(d2.createComment("cmt"));
        e3.appendChild(d2.createTextNode("b"));
        check("a".equals(a3.getWholeText()), "comment breaks wholeText adjacency");
        // replaceWholeText merges siblings into one Text
        org.w3c.dom.Element e4 = d2.createElement("e4");
        e2.appendChild(e4);
        org.w3c.dom.Text m1 = d2.createTextNode("1");
        e4.appendChild(m1);
        e4.appendChild(d2.createCDATASection("2"));
        e4.appendChild(d2.createTextNode("3"));
        org.w3c.dom.Text merged = m1.replaceWholeText("N");
        check(merged == m1 && "N".equals(m1.getData()), "replaceWholeText returns this node with new data");
        check(e4.getChildNodes().getLength() == 1 && org.w3c.dom.Node.TEXT_NODE == e4.getFirstChild().getNodeType(), "replaceWholeText collapses to single Text");
        check("N".equals(e4.getTextContent()), "replaceWholeText visible via parent textContent");
        org.w3c.dom.Text gone = m1.replaceWholeText("");
        check(gone == null, "replaceWholeText('') returns null (Xerces)");
        check(e4.getChildNodes().getLength() == 0, "replaceWholeText('') detaches the node");
        check(m1.getParentNode() == null, "replaceWholeText('') clears parent link");
        org.w3c.dom.Element e5 = d2.createElement("e5");
        e2.appendChild(e5);
        org.w3c.dom.Text n5 = d2.createTextNode("s");
        e5.appendChild(n5);
        org.w3c.dom.Text goneNull = n5.replaceWholeText(null);
        check(goneNull == null && e5.getChildNodes().getLength() == 0, "replaceWholeText(null) removes node (Xerces)");
        // normalize merges adjacent Text only, CDATA survives
        org.w3c.dom.Element e6 = d2.createElement("e6");
        e2.appendChild(e6);
        e6.appendChild(d2.createTextNode("a"));
        e6.appendChild(d2.createCDATASection("m"));
        e6.appendChild(d2.createTextNode("b"));
        e6.appendChild(d2.createTextNode("c"));
        e6.normalize();
        check(e6.getChildNodes().getLength() == 3, "normalize keeps CDATA as separator");
        check("a".equals(e6.getChildNodes().item(0).getNodeValue())
            && org.w3c.dom.Node.CDATA_SECTION_NODE == e6.getChildNodes().item(1).getNodeType()
            && "bc".equals(e6.getChildNodes().item(2).getNodeValue()), "normalize merges Text runs around CDATA");
        // isElementContentWhitespace default false without validation
        check(!d2.createTextNode(" ").isElementContentWhitespace(), "unvalidated text isElementContentWhitespace false");
        // document.normalize cascades
        d2.normalize();
        check("bc".equals(e6.getChildNodes().item(2).getNodeValue()), "document.normalize is recursive");
        // Text basics
        org.w3c.dom.Text plain = d.createTextNode("t");
        check("#text".equals(plain.getNodeName()), "text nodeName #text");
        check(plain.getAttributes() == null, "Text.getAttributes null");
        check(plain.isEqualNode(d2.createTextNode("t")) && !plain.isEqualNode(d2.createTextNode("u")), "text isEqualNode compares data");
        plain.setNodeValue(null);
        check(plain.getData() == null, "setNodeValue(null) -> null data");
    }
}
