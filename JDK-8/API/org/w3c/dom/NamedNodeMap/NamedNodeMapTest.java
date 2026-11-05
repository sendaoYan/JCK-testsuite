/*
 * @test
 * @summary org.w3c.dom.NamedNodeMap conformance: reflection shape checks.
 * @run main NamedNodeMapTest
 */

import java.lang.reflect.*;

public class NamedNodeMapTest {
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
        Class<?> c = Class.forName("org.w3c.dom.NamedNodeMap");
        check(c != null, "org.w3c.dom.NamedNodeMap loads");
        check(c.getName().equals("org.w3c.dom.NamedNodeMap"), "binary name is org.w3c.dom.NamedNodeMap");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.NamedNodeMap is public");
        check(c.isInterface(), "org.w3c.dom.NamedNodeMap is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getNamedItem"), "declares getNamedItem");
        check(declares(c, "setNamedItem"), "declares setNamedItem");
        check(declares(c, "removeNamedItem"), "declares removeNamedItem");
        check(declares(c, "item"), "declares item");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "getNamedItemNS"), "declares getNamedItemNS");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }



    // ---- deepening round 2: behavioral NamedNodeMap contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element e = d.createElement("e");
        e.setAttribute("a", "1");
        e.setAttribute("b", "2");
        org.w3c.dom.NamedNodeMap m = e.getAttributes();
        org.w3c.dom.NamedNodeMap mAgain = e.getAttributes();
        check(m == mAgain, "getAttributes returns the same instance (cached)");
        check(m.getLength() == 2, "initial attribute count");
        // live view
        e.setAttribute("c", "3");
        check(m.getLength() == 3, "map is live: setAttribute grows it");
        e.removeAttribute("c");
        check(m.getLength() == 2, "map is live: removeAttribute shrinks it");
        // lookup contract
        check(m.getNamedItem("a") != null && "1".equals(m.getNamedItem("a").getNodeValue()), "getNamedItem by name");
        check(m.getNamedItem("zz") == null, "getNamedItem(missing) null");
        check(m.getNamedItemNS("urn:none", "q") == null, "getNamedItemNS(missing) null");
        check(m.item(0).getNodeName().equals("a") && m.item(1).getNodeName().equals("b"), "iteration order is insertion order");
        check(m.item(2) == null, "item(past end) null");
        try {
            m.item(-1);
            check(false, "item(negative) must throw on Xerces NamedNodeMap");
        } catch (IndexOutOfBoundsException expected) {
            check(true, "item(-1) -> IndexOutOfBoundsException (AIOOBE on JDK 8, IOOBE on JDK 27)");
        }
        // setNamedItem: returns replaced node, or null
        org.w3c.dom.Attr repl = d.createAttribute("a");
        repl.setValue("one");
        org.w3c.dom.Node prev = m.setNamedItem(repl);
        check(prev != null && "1".equals(prev.getNodeValue()), "setNamedItem returns previous node");
        check("one".equals(e.getAttribute("a")), "setNamedItem replacement visible via element");
        check(m.getLength() == 2, "setNamedItem same-name does not grow map");
        check(((org.w3c.dom.Attr) prev).getOwnerElement() == null, "replaced attr detached from element");
        org.w3c.dom.Attr fresh = d.createAttribute("newk");
        fresh.setValue("nv");
        check(m.setNamedItem(fresh) == null, "setNamedItem(new name) returns null");
        check(m.getLength() == 3, "setNamedItem grows map");
        check(m.item(2).getNodeName().equals("newk"), "appended attr ordered last");
        // ns variants
        org.w3c.dom.Attr nsAttr = d.createAttributeNS("urn:p", "q:nsa");
        nsAttr.setValue("v");
        m.setNamedItemNS(nsAttr);
        check(m.getNamedItemNS("urn:p", "nsa") == nsAttr, "setNamedItemNS/getNamedItemNS round-trip");
        check(m.getLength() == 4, "ns attr counted");
        // removals
        org.w3c.dom.Node rm = m.removeNamedItem("b");
        check(rm.getNodeName().equals("b"), "removeNamedItem returns removed");
        check(m.getLength() == 3, "removeNamedItem shrinks");
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        try {
            m.removeNamedItem("b");
            check(false, "removeNamedItem(missing) must throw");
        } catch (org.w3c.dom.DOMException ex) {
            check(ex.code == org.w3c.dom.DOMException.NOT_FOUND_ERR, "removeNamedItem(missing) -> NOT_FOUND_ERR");
        }
        try {
            m.removeNamedItemNS("urn:none", "zz");
            check(false, "removeNamedItemNS(missing) must throw");
        } catch (org.w3c.dom.DOMException ex) {
            check(ex.code == org.w3c.dom.DOMException.NOT_FOUND_ERR, "removeNamedItemNS(missing) -> NOT_FOUND_ERR");
        }
        try {
            m.setNamedItem(other.createAttribute("foreign"));
            check(false, "setNamedItem(foreign) must throw");
        } catch (org.w3c.dom.DOMException ex) {
            check(ex.code == org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR, "setNamedItem(foreign attr) -> WRONG_DOCUMENT_ERR");
        }
        // attributes of a text node are null (not a map)
        check(d.createTextNode("t").getAttributes() == null, "Text.getAttributes null");
        // removal keeps element semantics in sync
        check(e.getAttribute("b").isEmpty(), "removed attr invisible via getAttribute");
        check(e.hasAttribute("a") && !e.hasAttribute("b"), "hasAttribute reflects map removal");
        // item() over a fresh clone is independent
        org.w3c.dom.NamedNodeMap cm = e.cloneNode(true).getAttributes();
        check(cm != m, "clone gets its own map");
        check(cm.getLength() == m.getLength(), "clone map mirrors attribute count");
    }

}
