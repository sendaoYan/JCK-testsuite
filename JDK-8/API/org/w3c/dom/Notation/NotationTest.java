/*
 * @test
 * @summary org.w3c.dom.Notation conformance: reflection shape checks.
 * @run main NotationTest
 */

import java.lang.reflect.*;

public class NotationTest {
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
        Class<?> c = Class.forName("org.w3c.dom.Notation");
        check(c != null, "org.w3c.dom.Notation loads");
        check(c.getName().equals("org.w3c.dom.Notation"), "binary name is org.w3c.dom.Notation");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.Notation is public");
        check(c.isInterface(), "org.w3c.dom.Notation is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getPublicId"), "declares getPublicId");
        check(declares(c, "getSystemId"), "declares getSystemId");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Notation contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        String xml = "<!DOCTYPE r [<!NOTATION gif PUBLIC 'gif pub' 'gif sys'>"
            + "<!NOTATION n2 SYSTEM 'n2 sys'>]><r/>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Notation gif = (org.w3c.dom.Notation) pd.getDoctype().getNotations().getNamedItem("gif");
        org.w3c.dom.Notation n2 = (org.w3c.dom.Notation) pd.getDoctype().getNotations().getNamedItem("n2");
        check(org.w3c.dom.Node.NOTATION_NODE == gif.getNodeType(), "notation nodeType 12");
        check("gif".equals(gif.getNodeName()), "notation nodeName is the declared name");
        check("gif pub".equals(gif.getPublicId()) && "gif sys".equals(gif.getSystemId()), "public notation records both ids");
        check(n2.getPublicId() == null && "n2 sys".equals(n2.getSystemId()), "system notation has null publicId");
        check(gif.getOwnerDocument() == pd && gif.getParentNode() == null, "notation owned by document, no parent");
        check(gif.getChildNodes().getLength() == 0 && gif.getFirstChild() == null, "notation is a leaf");
        check(gif.getNodeValue() == null, "notation nodeValue null");
        check(gif.getAttributes() == null, "notation getAttributes null");
        // Xerces silently ignores setNodeValue on notations
        gif.setNodeValue("x");
        check(gif.getNodeValue() == null, "notation setNodeValue is a silent no-op");
        // adoption refused (readonly node)
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        try { other.adoptNode(gif); check(false, "adoptNode(notation) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "adoptNode(notation) -> NO_MODIFICATION_ALLOWED_ERR"); }
        // notation map lookup misses and bounds
        check(pd.getDoctype().getNotations().getNamedItem("zz") == null, "notations getNamedItem(unknown) null");
        check(pd.getDoctype().getNotations().item(99) == null, "notations item(99) null");
        // factory doctype has no notations
        org.w3c.dom.Document blank = f.newDocumentBuilder().newDocument();
        check(blank.getImplementation().createDocumentType("q", null, null).getNotations().getLength() == 0, "factory doctype notations empty");
    }
}
