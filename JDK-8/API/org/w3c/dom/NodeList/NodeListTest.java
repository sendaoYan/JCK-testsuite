/*
 * @test
 * @summary org.w3c.dom.NodeList conformance: reflection shape checks.
 * @run main NodeListTest
 */

import java.lang.reflect.*;

public class NodeListTest {
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
        Class<?> c = Class.forName("org.w3c.dom.NodeList");
        check(c != null, "org.w3c.dom.NodeList loads");
        check(c.getName().equals("org.w3c.dom.NodeList"), "binary name is org.w3c.dom.NodeList");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.NodeList is public");
        check(c.isInterface(), "org.w3c.dom.NodeList is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "item"), "declares item");
        check(declares(c, "getLength"), "declares getLength");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }


    // ---- deepening round 2: behavioral NodeList contract (live view, OOB, wildcard scan) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element r = d.createElement("root");
        d.appendChild(r);
        org.w3c.dom.Element a = d.createElement("a");
        org.w3c.dom.Element b = d.createElement("b");
        r.appendChild(a);
        r.appendChild(b);

        // live view: mutations after obtaining the list are reflected
        org.w3c.dom.NodeList live = r.getChildNodes();
        check(live.getLength() == 2, "childNodes initial length");
        r.appendChild(d.createElement("c"));
        check(live.getLength() == 3, "childNodes reflects appends (live)");
        live.item(2).getParentNode().removeChild(live.item(2));
        check(live.getLength() == 2, "childNodes reflects removals (live)");
        // item bounds
        check(live.item(0) == a && live.item(1) == b, "item(i) returns children in order");
        check(live.item(2) == null, "item(past end) null");
        check(live.item(-1) == null, "item(negative) null");
        check(live.item(9999) == null, "item(far past end) null");

        // getElementsByTagName returns a live, recursive list
        org.w3c.dom.Element nested = d.createElement("a");
        b.appendChild(nested);
        org.w3c.dom.NodeList byTag = r.getElementsByTagName("a");
        check(byTag.getLength() == 2, "getElementsByTagName recursive match");
        r.appendChild(d.createElement("a"));
        check(byTag.getLength() == 3, "getElementsByTagName live on append");
        // wildcard
        org.w3c.dom.NodeList star = r.getElementsByTagName("*");
        check(star.getLength() == 4, "element-level wildcard excludes self");
        boolean selfAbsent = true;
        for (int i = 0; i < star.getLength(); i++) if (star.item(i) == r) selfAbsent = false;
        check(selfAbsent, "element wildcard never returns the context element");
        org.w3c.dom.NodeList docStar = d.getElementsByTagName("*");
        boolean rootIncluded = false;
        for (int i = 0; i < docStar.getLength(); i++) if (docStar.item(i) == r) rootIncluded = true;
        check(rootIncluded, "document wildcard includes documentElement");
        check(d.getElementsByTagName("a").getLength() == 3, "document-level tag scan");
        // removal reflected through old reference
        b.removeChild(nested);
        check(byTag.getLength() == 2, "getElementsByTagName live on removal");
        // namespaced scan
        org.w3c.dom.Element nsChild = d.createElementNS("urn:x", "p:a");
        r.appendChild(nsChild);
        check(r.getElementsByTagName("a").getLength() == 2, "non-ns tag scan does not match ns element");
        check(r.getElementsByTagNameNS("urn:x", "a").getLength() == 1, "ns scan matches ns element");
        check(r.getElementsByTagNameNS("*", "a").getLength() == 1, "ns wildcard-local only matches ns nodes (Xerces)");
        check(r.getElementsByTagNameNS("*", "*").getLength() == 4, "full wildcard ns scan");
        // empty result list
        org.w3c.dom.NodeList none = r.getElementsByTagName("zzz");
        check(none.getLength() == 0 && none.item(0) == null, "no-match list empty");
    }

}
