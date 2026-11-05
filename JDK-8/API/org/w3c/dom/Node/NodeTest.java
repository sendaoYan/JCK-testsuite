/*
 * @test
 * @summary org.w3c.dom.Node conformance: node-type constants, tree mutation, clone/normalize/textContent, compareDocumentPosition, equality, namespace lookup.
 * @run main NodeTest
 */

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class NodeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean domCode(Thrower t, short code) {
        try { t.run(); return false; }
        catch (DOMException e) { return e.code == code; }
        catch (Exception e) { return false; }
    }
    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }

    static Document newDoc() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().newDocument();
    }

    public static void main(String[] args) throws Exception {
        // ---- node-type constants (DOM Level 3 values) ----
        check(Node.ELEMENT_NODE == 1, "ELEMENT_NODE == 1");
        check(Node.ATTRIBUTE_NODE == 2, "ATTRIBUTE_NODE == 2");
        check(Node.TEXT_NODE == 3, "TEXT_NODE == 3");
        check(Node.CDATA_SECTION_NODE == 4, "CDATA_SECTION_NODE == 4");
        check(Node.ENTITY_REFERENCE_NODE == 5, "ENTITY_REFERENCE_NODE == 5");
        check(Node.ENTITY_NODE == 6, "ENTITY_NODE == 6");
        check(Node.PROCESSING_INSTRUCTION_NODE == 7, "PROCESSING_INSTRUCTION_NODE == 7");
        check(Node.COMMENT_NODE == 8, "COMMENT_NODE == 8");
        check(Node.DOCUMENT_NODE == 9, "DOCUMENT_NODE == 9");
        check(Node.DOCUMENT_TYPE_NODE == 10, "DOCUMENT_TYPE_NODE == 10");
        check(Node.DOCUMENT_FRAGMENT_NODE == 11, "DOCUMENT_FRAGMENT_NODE == 11");
        check(Node.NOTATION_NODE == 12, "NOTATION_NODE == 12");

        Document d = newDoc();
        Element root = d.createElement("root");
        d.appendChild(root);

        // ---- node name / type / value ----
        check(d.getNodeType() == Node.DOCUMENT_NODE, "document nodeType");
        check("#document".equals(d.getNodeName()), "document nodeName");
        check(d.getNodeValue() == null, "document nodeValue null");
        check(root.getNodeType() == Node.ELEMENT_NODE, "element nodeType");
        check("root".equals(root.getNodeName()), "element nodeName");
        check(root.getNodeValue() == null, "element nodeValue null");
        check(root.getNodeName().equals(root.getTagName()), "element nodeName == tagName");
        // setNodeValue on element is a no-op, remains null
        root.setNodeValue("ignored");
        check(root.getNodeValue() == null, "element setNodeValue is no-op");

        // text node name/value
        Text txt = d.createTextNode("hello");
        check(txt.getNodeType() == Node.TEXT_NODE, "text nodeType");
        check("#text".equals(txt.getNodeName()), "text nodeName");
        check("hello".equals(txt.getNodeValue()), "text nodeValue");
        txt.setNodeValue("world");
        check("world".equals(txt.getData()), "text setNodeValue updates data");

        // ---- tree mutation: append/insert/replace/remove ----
        Element a = d.createElement("a");
        Element b = d.createElement("b");
        Element c = d.createElement("c");
        Node ra = root.appendChild(a);
        check(ra == a, "appendChild returns appended node");
        root.appendChild(b);
        root.appendChild(c);
        check(root.getFirstChild() == a && root.getLastChild() == c, "first/last child");
        check(a.getNextSibling() == b && b.getPreviousSibling() == a, "next/previous sibling");
        check(a.getParentNode() == root, "parentNode");
        // insertBefore at front
        Element z = d.createElement("z");
        root.insertBefore(z, a);
        check(root.getFirstChild() == z, "insertBefore front");
        check(z.getNextSibling() == a, "insertBefore ordering");
        // insertBefore with null ref == append
        Element tail = d.createElement("tail");
        root.insertBefore(tail, null);
        check(root.getLastChild() == tail, "insertBefore(null ref) appends");
        // replaceChild returns old
        Element nw = d.createElement("nw");
        Node old = root.replaceChild(nw, z);
        check(old == z && root.getFirstChild() == nw, "replaceChild returns old and replaces");
        // removeChild returns removed, removes from tree
        Node removed = root.removeChild(nw);
        check(removed == nw && nw.getParentNode() == null, "removeChild detaches");
        check(domCode(() -> root.removeChild(d.createElement("stranger")), DOMException.NOT_FOUND_ERR), "removeChild(non-child) -> NOT_FOUND_ERR");
        check(isNpe(() -> root.appendChild(null)), "appendChild(null) -> NPE");

        // ---- childNodes is a live NodeList ----
        NodeList live = root.getChildNodes();
        int before = live.getLength();
        root.appendChild(d.createElement("extra"));
        check(live.getLength() == before + 1, "childNodes is live");
        check(live.item(before) == root.getLastChild(), "live childNodes item");

        // ---- cloneNode shallow vs deep ----
        Element parent = d.createElement("parent");
        parent.appendChild(d.createTextNode("T"));
        Element kid = d.createElement("kid");
        parent.appendChild(kid);
        Node shallow = parent.cloneNode(false);
        check(shallow.getChildNodes().getLength() == 0, "cloneNode(false) shallow");
        check(shallow.getNodeType() == Node.ELEMENT_NODE && "parent".equals(shallow.getNodeName()), "cloneNode preserves type/name");
        Node deep = parent.cloneNode(true);
        check(deep.getChildNodes().getLength() == 2, "cloneNode(true) deep");
        check("kid".equals(deep.getLastChild().getNodeName()), "cloneNode deep child");

        // ---- normalize merges adjacent text ----
        Element e = d.createElement("e");
        e.appendChild(d.createTextNode("ab"));
        e.appendChild(d.createTextNode("cd"));
        check(e.getChildNodes().getLength() == 2, "two text children before normalize");
        e.normalize();
        check(e.getChildNodes().getLength() == 1, "normalize merges text nodes");
        check("abcd".equals(e.getFirstChild().getNodeValue()), "normalize concatenates text");

        // ---- text content ----
        Element e2 = d.createElement("e2");
        e2.appendChild(d.createTextNode("hello"));
        e2.appendChild(d.createElement("sub"));
        check("hello".equals(e2.getTextContent()), "getTextContent concatenates text descendants");
        e2.setTextContent("replaced");
        check(e2.getChildNodes().getLength() == 1, "setTextContent replaces children with one text node");
        check("replaced".equals(e2.getTextContent()), "setTextContent value");
        check(e2.getFirstChild().getNodeType() == Node.TEXT_NODE, "setTextContent creates text node");

        // ---- compareDocumentPosition ----
        Element parent2 = d.createElement("p2");
        Element child2 = d.createElement("c2");
        parent2.appendChild(child2);
        check((parent2.compareDocumentPosition(child2) & Node.DOCUMENT_POSITION_CONTAINED_BY) != 0, "parent contains child (CONTAINED_BY)");
        check((child2.compareDocumentPosition(parent2) & Node.DOCUMENT_POSITION_CONTAINS) != 0, "child contained by parent (CONTAINS)");
        Element sibling1 = d.createElement("s1");
        Element sibling2 = d.createElement("s2");
        Element holder = d.createElement("h");
        holder.appendChild(sibling1);
        holder.appendChild(sibling2);
        check((sibling1.compareDocumentPosition(sibling2) & Node.DOCUMENT_POSITION_FOLLOWING) != 0, "later sibling FOLLOWING");

        // ---- equality ----
        check(parent2.isSameNode(parent2), "isSameNode self");
        check(!parent2.isSameNode(child2), "isSameNode distinct false");
        Element twin = (Element) parent2.cloneNode(true);
        check(parent2.isEqualNode(twin), "isEqualNode deep-equal clone");
        check(!parent2.isEqualNode(child2), "isEqualNode false for distinct");

        // ---- namespace direct properties ----
        Element ne = d.createElementNS("urn:ns", "p:item");
        check("urn:ns".equals(ne.getNamespaceURI()), "namespace URI");
        check("item".equals(ne.getLocalName()), "local name");
        check("p".equals(ne.getPrefix()), "prefix");
        check("p:item".equals(ne.getNodeName()), "qualified node name");
        ne.setPrefix("q");
        check("q".equals(ne.getPrefix()), "setPrefix");
        check("q:item".equals(ne.getNodeName()), "setPrefix updates nodeName");

        // ---- namespace lookup on parsed document ----
        DocumentBuilderFactory pf = DocumentBuilderFactory.newInstance();
        pf.setNamespaceAware(true);
        Document pd = pf.newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root xmlns:p=\"urn:ns\"><p:item/></root>".getBytes("UTF-8")));
        Element item = (Element) pd.getElementsByTagNameNS("urn:ns", "item").item(0);
        check("urn:ns".equals(item.lookupNamespaceURI("p")), "lookupNamespaceURI resolves declared prefix");
        check("p".equals(item.lookupPrefix("urn:ns")), "lookupPrefix resolves declared namespace");
        check(!item.isDefaultNamespace("urn:ns"), "isDefaultNamespace false for prefixed element");

        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: full mutation / position / identity / userData contracts ----
    static void deepContract() throws Exception {
        DocumentBuilderFactory nf = DocumentBuilderFactory.newInstance();
        nf.setNamespaceAware(true);
        Document dd = nf.newDocumentBuilder().newDocument();
        Element r = dd.createElement("root");
        dd.appendChild(r);
        Element a = dd.createElement("a");
        Element b = dd.createElement("b");
        Element c = dd.createElement("c");
        r.appendChild(a);
        r.appendChild(b);
        r.appendChild(c);

        // DOCUMENT_POSITION constants are exact DOM Level 3 values
        check(Node.DOCUMENT_POSITION_DISCONNECTED == 1, "DOCUMENT_POSITION_DISCONNECTED == 1");
        check(Node.DOCUMENT_POSITION_PRECEDING == 2, "DOCUMENT_POSITION_PRECEDING == 2");
        check(Node.DOCUMENT_POSITION_FOLLOWING == 4, "DOCUMENT_POSITION_FOLLOWING == 4");
        check(Node.DOCUMENT_POSITION_CONTAINS == 8, "DOCUMENT_POSITION_CONTAINS == 8");
        check(Node.DOCUMENT_POSITION_CONTAINED_BY == 16, "DOCUMENT_POSITION_CONTAINED_BY == 16");
        check(Node.DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC == 32, "DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC == 32");

        // precise masks (Xerces actual values, identical on JDK 8 / JDK 27)
        Element leaf = dd.createElement("leaf");
        a.appendChild(leaf);
        check(r.compareDocumentPosition(a) == 20, "ancestor->descendant mask 20 (FOLLOWING|CONTAINED_BY)");
        check(a.compareDocumentPosition(r) == 10, "descendant->ancestor mask 10 (PRECEDING|CONTAINS)");
        check(a.compareDocumentPosition(b) == 4, "earlier vs later sibling == 4");
        check(b.compareDocumentPosition(a) == 2, "later vs earlier sibling == 2");
        check(a.compareDocumentPosition(a) == 0, "node vs itself == 0");
        Element orphan = dd.createElement("orphan");
        int dpos = a.compareDocumentPosition(orphan);
        check(dpos == 37, "disconnected forward == 37 (DISCONNECTED|IMPL_SPECIFIC|FOLLOWING)");
        check(orphan.compareDocumentPosition(a) == 35, "disconnected backward == 35 (DISCONNECTED|IMPL_SPECIFIC|PRECEDING)");
        check((dpos & Node.DOCUMENT_POSITION_DISCONNECTED) != 0, "disconnected result has DISCONNECTED bit");

        // appendChild MOVES an already-attached node (no duplication)
        r.appendChild(a); // a was first child; moves to end
        check(r.getChildNodes().getLength() == 3, "appendChild(existing) keeps count (move)");
        check(r.getFirstChild() == b && r.getLastChild() == a, "appendChild(existing) relocates node to end");
        // cycle / self insertion
        check(domCode(() -> a.appendChild(r), DOMException.HIERARCHY_REQUEST_ERR), "appending own ancestor -> HIERARCHY_REQUEST_ERR");
        check(domCode(() -> a.appendChild(a), DOMException.HIERARCHY_REQUEST_ERR), "appending node to itself -> HIERARCHY_REQUEST_ERR");
        // insertBefore(x, x) is a permitted no-op; replaceChild(x, x) returns x
        r.insertBefore(a, a);
        check(r.getChildNodes().getLength() == 3, "insertBefore(x,x) no-op keeps count");
        check(r.replaceChild(a, a) == a, "replaceChild(x,x) returns same node");
        // replaceChild with an in-tree new node: old detached, new moved (not duplicated)
        Element nw = dd.createElement("nw");
        r.appendChild(nw);
        Node oldReplaced = r.replaceChild(nw, c);
        check(oldReplaced == c, "replaceChild returns old node");
        check(c.getParentNode() == null, "replaced old node detached");
        check(nw.getParentNode() == r, "in-tree new node moved into slot");
        // removing the document element
        Node removedRoot = dd.removeChild(r);
        check(removedRoot == r && dd.getDocumentElement() == null, "removeChild(documentElement) -> getDocumentElement null");
        dd.appendChild(r);
        check(dd.getDocumentElement() == r, "re-appending root restores documentElement");
        // insertBefore error contract
        check(isNpe(() -> r.insertBefore(null, a)), "insertBefore(null, ref) -> NPE");
        check(domCode(() -> r.insertBefore(a, dd.createElement("missing")), DOMException.NOT_FOUND_ERR), "insertBefore(x, missingRef) -> NOT_FOUND_ERR");
        // hierarchy errors
        check(domCode(() -> dd.createTextNode("t").appendChild(a), DOMException.HIERARCHY_REQUEST_ERR), "appendChild on Text -> HIERARCHY_REQUEST_ERR");
        check(domCode(() -> dd.createTextNode("t").insertBefore(a, null), DOMException.HIERARCHY_REQUEST_ERR), "insertBefore on Text -> HIERARCHY_REQUEST_ERR");

        // cross-document insertion
        Document other = nf.newDocumentBuilder().newDocument();
        check(domCode(() -> r.appendChild(other.createElement("foreign")), DOMException.WRONG_DOCUMENT_ERR), "appendChild(foreign doc node) -> WRONG_DOCUMENT_ERR");

        // cloneNode details
        Element attrHolder = dd.createElement("attrHolder");
        attrHolder.setAttribute("k", "v");
        Element shallow = (Element) attrHolder.cloneNode(false);
        check("v".equals(shallow.getAttribute("k")), "shallow clone keeps attributes");
        check(shallow.getParentNode() == null, "clone has null parent");
        check(shallow.getOwnerDocument() == dd, "clone keeps ownerDocument");
        a.appendChild(dd.createTextNode("one"));
        Element deepHost = dd.createElement("deepHost");
        deepHost.appendChild(a);
        Element deepClone = (Element) deepHost.cloneNode(true);
        check(deepClone.getFirstChild() != a, "deep clone children are distinct instances");
        check(deepClone.isEqualNode(deepHost), "deep clone is isEqualNode-equal");
        attrHolder.setUserData("u", "payload", null);
        Node clonedUser = attrHolder.cloneNode(true);
        check(clonedUser.getUserData("u") == null, "cloneNode does not copy userData (no handler)");
        // equality contracts
        check(isNpe(() -> attrHolder.isEqualNode(null)), "isEqualNode(null) -> NPE");
        check(!attrHolder.isSameNode(null), "isSameNode(null) == false");
        Element eqA = dd.createElement("eq");
        eqA.appendChild(dd.createTextNode("x"));
        Element eqB = dd.createElement("eq");
        eqB.appendChild(dd.createTextNode("x"));
        Element eqC = dd.createElement("eq");
        eqC.appendChild(dd.createTextNode("y"));
        check(eqA.isEqualNode(eqB), "same-shape trees isEqualNode");
        check(!eqA.isEqualNode(eqC), "different text content breaks isEqualNode");
        Element eqD = dd.createElement("eq");
        eqD.setAttribute("extra", "1");
        check(!eqA.isEqualNode(eqD), "attribute difference breaks isEqualNode");
        Element foreignSame = other.createElement("eq");
        foreignSame.appendChild(other.createTextNode("x"));
        check(eqA.isEqualNode(foreignSame), "isEqualNode works across documents");

        // normalize: idempotent, absorbs empty text nodes
        Element nrm = dd.createElement("nrm");
        nrm.appendChild(dd.createTextNode(""));
        nrm.appendChild(dd.createTextNode("a"));
        nrm.appendChild(dd.createTextNode("b"));
        nrm.normalize();
        check(nrm.getChildNodes().getLength() == 1, "normalize merges adjacent text incl. empty");
        check("ab".equals(nrm.getFirstChild().getNodeValue()), "normalized content concatenates");
        nrm.normalize();
        check(nrm.getChildNodes().getLength() == 1, "normalize is idempotent");

        // textContent contracts
        check("cc".equals(dd.createComment("cc").getTextContent()), "comment textContent is its data");
        check("dd".equals(dd.createProcessingInstruction("t", "dd").getTextContent()), "PI textContent is its data");
        Attr tca = dd.createAttribute("ta");
        tca.setValue("av");
        check("av".equals(tca.getTextContent()), "attr textContent is its value");
        Document emptyDoc = nf.newDocumentBuilder().newDocument();
        check(emptyDoc.getTextContent() == null, "empty document textContent null");
        Element nullTc = dd.createElement("nullTc");
        nullTc.appendChild(dd.createTextNode("q"));
        nullTc.setTextContent(null);
        check(nullTc.getChildNodes().getLength() == 0, "setTextContent(null) removes children");
        check(nullTc.getTextContent().isEmpty(), "setTextContent(null) yields empty content");
        int docChildrenBefore = dd.getChildNodes().getLength();
        dd.setTextContent("ignored");
        check(dd.getChildNodes().getLength() == docChildrenBefore, "Document.setTextContent is a no-op");
        Text syncT = dd.createTextNode("a");
        syncT.setTextContent("b");
        check("b".equals(syncT.getNodeValue()), "Text.setTextContent syncs nodeValue");

        // hasAttributes
        Element bare = dd.createElement("bare");
        check(!bare.hasAttributes(), "bare element hasAttributes false");
        bare.setAttribute("x", "1");
        check(bare.hasAttributes(), "element with attr hasAttributes true");
        check(!dd.createTextNode("t").hasAttributes(), "text hasAttributes false");

        // userData: previous value, removal, handler callbacks with full parameters
        Element ud = dd.createElement("ud");
        ud.setAttribute("k0", "v0");
        check(ud.setUserData("k", "v1", null) == null, "setUserData returns null when absent");
        check("v1".equals(ud.getUserData("k")), "getUserData round-trip");
        check(ud.getUserData("missing") == null, "getUserData(missing) null");
        check(ud.getUserData(null) == null, "getUserData(null-key) null");
        check("v1".equals(ud.setUserData("k", null, null)), "setUserData(k,null) returns previous value");
        check(ud.getUserData("k") == null, "setUserData(k,null) clears entry");
        final java.util.List<String> udLog = new java.util.ArrayList<String>();
        ud.setUserData("h", "hv", new UserDataHandler() {
            public void handle(short op, String key, Object data, Node src, Node dst) {
                udLog.add(op + ":" + key + ":" + data + ":" + src.getNodeName() + ":" + (dst == null ? "null" : dst.getNodeName()));
            }
        });
        Node udClone = ud.cloneNode(true);
        check(udLog.get(0).equals("1:h:hv:ud:ud"), "clone fires NODE_CLONED with dst=clone");
        check(udClone.getNodeName().equals("ud"), "clone dst node name");
        dd.importNode(ud, true);
        check(udLog.get(1).equals("2:h:hv:ud:ud"), "import fires NODE_IMPORTED");
        dd.renameNode(ud, null, "udRenamed");
        check(udLog.get(2).equals("4:h:hv:udRenamed:null"), "rename fires NODE_RENAMED with dst null");
        other.adoptNode(ud);
        check(udLog.get(3).equals("5:h:hv:udRenamed:null"), "adopt fires NODE_ADOPTED with dst null");
        check("hv".equals(ud.getUserData("h")), "userData travels with adopted node");
        check(UserDataHandler.NODE_CLONED == 1 && UserDataHandler.NODE_IMPORTED == 2 && UserDataHandler.NODE_DELETED == 3
            && UserDataHandler.NODE_RENAMED == 4 && UserDataHandler.NODE_ADOPTED == 5, "UserDataHandler operation constants");

        // namespace lookup on a parsed document (default + prefixed)
        Document pd = nf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
            "<r xmlns='urn:def' xmlns:p='urn:p'><p:item/></r>".getBytes("UTF-8")));
        Element pitem = (Element) pd.getDocumentElement().getFirstChild();
        check("p".equals(pitem.lookupPrefix("urn:p")), "lookupPrefix resolves prefixed mapping");
        check("urn:p".equals(pitem.lookupNamespaceURI("p")), "lookupNamespaceURI resolves prefix");
        check("urn:def".equals(pitem.lookupNamespaceURI(null)), "null prefix resolves default namespace");
        check(pitem.isDefaultNamespace("urn:def"), "isDefaultNamespace true for default");
        check(!pitem.isDefaultNamespace("urn:p"), "isDefaultNamespace false for non-default");
        check(pitem.lookupPrefix("urn:none") == null, "lookupPrefix(unknown) null");
        check(pd.getDocumentElement().lookupNamespaceURI("xml") == null, "Xerces lookupNamespaceURI(xml) null without declaration");

        // base URI / isSupported / setPrefix negatives
        pd.setDocumentURI("http://ex.test/doc.xml");
        check("http://ex.test/doc.xml".equals(pd.getDocumentElement().getBaseURI()), "getBaseURI inherits documentURI");
        check(pd.getDocumentElement().isSupported("Core", "2.0"), "isSupported(Core,2.0) true");
        check(!pd.getDocumentElement().isSupported("bogus", "1.0"), "isSupported(bogus) false");
        Element pfx = pd.createElementNS("urn:x", "q:item");
        check(domCode(() -> pfx.setPrefix("bad name"), DOMException.INVALID_CHARACTER_ERR), "setPrefix(illegal) -> INVALID_CHARACTER_ERR");
        check(domCode(() -> pd.createElement("plain").setPrefix("p"), DOMException.NAMESPACE_ERR), "setPrefix on non-ns node -> NAMESPACE_ERR");

        // importNode family
        Attr impAttr = (Attr) other.importNode(dd.createAttribute("ia"), true);
        check("ia".equals(impAttr.getName()) && impAttr.getOwnerDocument() == other, "importNode(Attr) rebinding");
        Comment impComment = (Comment) other.importNode(dd.createComment("ic"), true);
        check("ic".equals(impComment.getData()), "importNode(Comment)");
        DocumentFragment frag = dd.createDocumentFragment();
        frag.appendChild(dd.createElement("fe"));
        DocumentFragment impFrag = (DocumentFragment) other.importNode(frag, true);
        check(impFrag.getChildNodes().getLength() == 1, "importNode(DocumentFragment) deep");
    }

}
