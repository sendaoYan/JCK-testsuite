/*
 * @test
 * @summary org.w3c.dom.Attr conformance: reflection shape checks.
 * @run main AttrTest
 */

import java.lang.reflect.*;

public class AttrTest {
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
        Class<?> c = Class.forName("org.w3c.dom.Attr");
        check(c != null, "org.w3c.dom.Attr loads");
        check(c.getName().equals("org.w3c.dom.Attr"), "binary name is org.w3c.dom.Attr");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.Attr is public");
        check(c.isInterface(), "org.w3c.dom.Attr is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "getSpecified"), "declares getSpecified");
        check(declares(c, "getValue"), "declares getValue");
        check(declares(c, "setValue"), "declares setValue");
        check(declares(c, "getOwnerElement"), "declares getOwnerElement");
        check(declares(c, "getSchemaTypeInfo"), "declares getSchemaTypeInfo");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }




    // ---- deepening round 2: attr value model / owner lifecycle / DTD isId / children model ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();

        // factory-created attr state before mounting
        org.w3c.dom.Attr a = d.createAttribute("free");
        check("free".equals(a.getName()) && "free".equals(a.getNodeName()), "attr name via getName/getNodeName");
        check(a.getOwnerElement() == null, "unmounted attr ownerElement null");
        check(a.getOwnerDocument() == d, "attr ownerDocument set");
        check(a.getSpecified(), "factory attr specified true");
        check(a.getOwnerElement() == null && a.getParentNode() == null, "attr has no parent node");
        // value model: value and child text stay in sync
        a.setValue("v1");
        check("v1".equals(a.getValue()) && "v1".equals(a.getNodeValue()), "setValue visible via getValue/getNodeValue");
        check(a.getChildNodes().getLength() == 1 && a.getFirstChild().getNodeType() == org.w3c.dom.Node.TEXT_NODE, "attr value exposed as text child");
        a.setNodeValue("v2");
        check("v2".equals(a.getValue()), "setNodeValue syncs getValue");
        a.setTextContent("v3");
        check("v3".equals(a.getValue()), "setTextContent syncs getValue");
        a.setValue("v4");
        check("v4".equals(a.getTextContent()), "getValue syncs getTextContent");
        // appending extra text extends the value
        a.appendChild(d.createTextNode("+"));
        check("v4+".equals(a.getValue()), "attr value concatenates text children");
        check(a.getChildNodes().getLength() == 2, "two text children after append");
        // character-data writes on the attr's text child are allowed (not read-only)
        org.w3c.dom.Attr wa = d.createAttribute("wa");
        wa.setValue("base");
        ((org.w3c.dom.Text) wa.getFirstChild()).setData("mod");
        check("mod".equals(wa.getValue()), "attr text child is writable");
        wa.getFirstChild().setNodeValue("nn");
        check("nn".equals(wa.getValue()), "attr text setNodeValue writable");
        // Xerces lazily exposes the text child of a valueless attr
        org.w3c.dom.Attr lazy = d.createAttribute("lazy");
        check(lazy.getFirstChild() == null, "fresh attr getFirstChild null (lazy)");
        lazy.setValue("");
        check(lazy.getFirstChild() != null && lazy.getFirstChild().getNodeValue().isEmpty(), "setValue('') materializes empty text child");
        // mounting lifecycle
        org.w3c.dom.Element e = d.createElement("e");
        e.setAttributeNode(a);
        check(a.getOwnerElement() == e, "mounted attr ownerElement points to element");
        check(e.getAttributeNode("free") == a, "element sees mounted attr");
        org.w3c.dom.Attr taken = e.removeAttributeNode(a);
        check(taken == a && a.getOwnerElement() == null, "unmounted attr ownerElement cleared");
        // clone independence
        org.w3c.dom.Attr ca = (org.w3c.dom.Attr) a.cloneNode(true);
        check(ca.getValue().equals(a.getValue()), "clone keeps value");
        ca.setValue("other");
        check(!a.getValue().equals("other"), "clone value independent");
        // importNode rebinding
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Attr imported = (org.w3c.dom.Attr) other.importNode(a, true);
        check(imported.getOwnerDocument() == other && imported.getValue().equals(a.getValue()), "imported attr rebinding");
        // spec: schema type info reachable, isId false without DTD
        check(!a.isId(), "manual attr isId false without DTD");
        check(a.getSchemaTypeInfo() != null, "attr getSchemaTypeInfo non-null");

        // DTD-backed semantics: declared ID attribute and default attribute
        String xml = "<!DOCTYPE r [<!ELEMENT r ANY><!ATTLIST item id ID #IMPLIED>"
            + "<!ATTLIST item def CDATA 'dflt'>]><r><item id='x1' def='user'/><item/></r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Element item1 = (org.w3c.dom.Element) pd.getElementsByTagName("item").item(0);
        org.w3c.dom.Attr idAttr = item1.getAttributeNode("id");
        check(idAttr.isId(), "DTD-declared ID attr isId true");
        check(pd.getElementById("x1") == item1, "getElementById resolves DTD ID");
        check(pd.getElementById("zzz") == null, "getElementById(unknown) null");
        check("ID".equals(idAttr.getSchemaTypeInfo().getTypeName()), "ID attr typeName 'ID'");
        check(idAttr.getSpecified(), "explicitly given attr specified true");
        check("user".equals(idAttr.getOwnerElement().getAttribute("def")), "explicit default-attr value wins");
        org.w3c.dom.Element item2 = (org.w3c.dom.Element) pd.getElementsByTagName("item").item(1);
        check(!item2.hasAttribute("id"), "absent ID attr not fabricated");
        org.w3c.dom.Attr defAttr = item2.getAttributeNode("def");
        check(defAttr != null && "dflt".equals(defAttr.getValue()), "default attr injected from DTD");
        check(!defAttr.getSpecified(), "DTD default attr specified false");
        // attr on ns element
        org.w3c.dom.Element nsEl = pd.createElementNS("urn:x", "p:n");
        org.w3c.dom.Attr nsA = pd.createAttributeNS("urn:y", "q:k");
        nsA.setValue("1");
        nsEl.setAttributeNodeNS(nsA);
        check("urn:y".equals(nsA.getNamespaceURI()) && "k".equals(nsA.getLocalName()) && "q".equals(nsA.getPrefix()), "ns attr name decomposition");
        check(nsEl.getAttributeNodeNS("urn:y", "k") == nsA, "ns attr retrieval by uri+local");
        // attr of an attr-less document fragment state: attributes map is null on Attr itself
        check(a.getAttributes() == null, "Attr.getAttributes null");
        // getNodeType/getNodeValue defaults
        check(a.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE, "attr nodeType 2");
        a.setValue(null);
        check(a.getValue().isEmpty() && a.getFirstChild() == null, "setValue(null) empties value (empty string, child cleared)");
    }

}
