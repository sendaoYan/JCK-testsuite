/*
 * @test
 * @summary org.w3c.dom.CDATASection conformance: reflection shape checks.
 * @run main CDATASectionTest
 */

import java.lang.reflect.*;

public class CDATASectionTest {
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
        Class<?> c = Class.forName("org.w3c.dom.CDATASection");
        check(c != null, "org.w3c.dom.CDATASection loads");
        check(c.getName().equals("org.w3c.dom.CDATASection"), "binary name is org.w3c.dom.CDATASection");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.CDATASection is public");
        check(c.isInterface(), "org.w3c.dom.CDATASection is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Text")), "direct superinterfaces");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral CDATASection contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setCoalescing(false);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.CDATASection c = d.createCDATASection("xy");
        check("#cdata-section".equals(c.getNodeName()), "cdata nodeName #cdata-section");
        check(org.w3c.dom.Node.CDATA_SECTION_NODE == c.getNodeType(), "cdata nodeType 4");
        check(c instanceof org.w3c.dom.Text, "CDATASection is a Text");
        check("xy".equals(c.getData()) && "xy".equals(c.getNodeValue()) && "xy".equals(c.getTextContent()), "cdata value channels agree");
        check(c.getLength() == 2, "cdata getLength");
        // CharacterData interface fully inherited
        c.setData("abc");
        c.appendData("d");
        check("abcd".equals(c.getData()), "cdata appendData inherited");
        check("bc".equals(c.substringData(1, 2)), "cdata substringData inherited");
        c.replaceData(0, 4, "z");
        check("z".equals(c.getData()), "cdata replaceData inherited");
        // splitText on CDATA: the new half is a plain Text node
        org.w3c.dom.Element e = d.createElement("e");
        d.appendChild(e);
        org.w3c.dom.CDATASection sc = d.createCDATASection("xy");
        e.appendChild(sc);
        org.w3c.dom.Node newHalf = sc.splitText(1);
        check("x".equals(sc.getData()) && "y".equals(newHalf.getNodeValue()), "cdata splitText halves data");
        check(org.w3c.dom.Node.TEXT_NODE == newHalf.getNodeType() && "#text".equals(newHalf.getNodeName()), "cdata splitText produces a plain Text sibling (not CDATA)");
        org.w3c.dom.CDATASection sc2 = d.createCDATASection("m");
        e.appendChild(sc2);
        org.w3c.dom.Node all = sc2.splitText(0);
        check("".equals(sc2.getData()) && "m".equals(all.getNodeValue()) && org.w3c.dom.Node.TEXT_NODE == all.getNodeType(), "cdata splitText(0) moves all into new Text");
        // wholeText family works through the Text view
        org.w3c.dom.Element e2 = d.createElement("e2");
        e.appendChild(e2);
        org.w3c.dom.CDATASection w = d.createCDATASection("m");
        e2.appendChild(w);
        check("m".equals(w.getWholeText()), "cdata getWholeText returns its data");
        org.w3c.dom.Text merged = w.replaceWholeText("N");
        check(e2.getChildNodes().getLength() == 1 && merged == w, "cdata replaceWholeText collapses to the same single node");
        check(org.w3c.dom.Node.CDATA_SECTION_NODE == merged.getNodeType() && "N".equals(merged.getData()), "cdata replaceWholeText keeps CDATA type and sets data");
        // adjacency in wholeText scanning
        org.w3c.dom.Element e3 = d.createElement("e3");
        e.appendChild(e3);
        org.w3c.dom.Text lead = d.createTextNode("a");
        e3.appendChild(lead);
        e3.appendChild(d.createCDATASection("b"));
        e3.appendChild(d.createTextNode("c"));
        check("abc".equals(lead.getWholeText()), "cdata participates in wholeText span");
        // ']]>' is storable in the DOM model (serialization handled elsewhere)
        org.w3c.dom.CDATASection term = d.createCDATASection("a]]>b");
        check("a]]>b".equals(term.getData()), "createCDATASection accepts ']]>' content");
        // parse: CDATA content is not entity- or markup-escaped
        String xml = "<!DOCTYPE r [<!ENTITY ent \"E\">]><r><t>A<![CDATA[x < y & z]]>B&amp;C&ent;</t><e><![CDATA[]]></e></r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Element t2 = (org.w3c.dom.Element) pd.getElementsByTagName("t").item(0);
        check("Ax < y & zB&CE".equals(t2.getTextContent()), "cdata content stays raw while surroundings expand");
        org.w3c.dom.CDATASection pcd = (org.w3c.dom.CDATASection) t2.getChildNodes().item(1);
        check("x < y & z".equals(pcd.getData()), "parsed cdata data unescaped");
        org.w3c.dom.Element e4 = (org.w3c.dom.Element) pd.getElementsByTagName("e").item(0);
        org.w3c.dom.CDATASection empty = (org.w3c.dom.CDATASection) e4.getFirstChild();
        check(empty.getLength() == 0 && "".equals(empty.getData()), "empty cdata section parses to zero length");
        // equality and clone
        org.w3c.dom.CDATASection c1 = d.createCDATASection("x");
        org.w3c.dom.CDATASection c2 = d.createCDATASection("x");
        check(c1.isEqualNode(c2), "cdata isEqualNode same data");
        check(!c1.isEqualNode(d.createTextNode("x")), "cdata never equals a Text node");
        check("x".equals(((org.w3c.dom.CDATASection) c1.cloneNode(true)).getData()), "cdata clone keeps data");
        check(c.getAttributes() == null, "cdata getAttributes null");
    }
}
