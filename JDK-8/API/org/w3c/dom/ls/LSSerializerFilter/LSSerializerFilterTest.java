/*
 * @test
 * @summary org.w3c.dom.ls.LSSerializerFilter conformance: reflection shape checks.
 * @run main LSSerializerFilterTest
 */

import java.lang.reflect.*;

public class LSSerializerFilterTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ls.LSSerializerFilter");
        check(c != null, "org.w3c.dom.ls.LSSerializerFilter loads");
        check(c.getName().equals("org.w3c.dom.ls.LSSerializerFilter"), "binary name is org.w3c.dom.ls.LSSerializerFilter");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSSerializerFilter is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSSerializerFilter is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.traversal.NodeFilter")), "direct superinterfaces");
        check(declares(c, "getWhatToShow"), "declares getWhatToShow");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LSSerializerFilter contract ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.traversal.NodeFilter.class.isAssignableFrom(org.w3c.dom.ls.LSSerializerFilter.class), "LSSerializerFilter extends NodeFilter");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.ls.LSSerializerFilter.class.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getWhatToShow"), "only getWhatToShow declared (verdicts inherited from NodeFilter)");
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element r = d.createElement("r"); d.appendChild(r);
        org.w3c.dom.Element a = d.createElement("a"); a.appendChild(d.createTextNode("T"));
        r.appendChild(a); r.appendChild(d.createElement("b"));
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) d.getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSSerializer ser = ls.createLSSerializer();
        org.w3c.dom.ls.LSSerializerFilter flt = new org.w3c.dom.ls.LSSerializerFilter() {
            public short acceptNode(org.w3c.dom.Node n) { return "a".equals(n.getNodeName()) ? org.w3c.dom.ls.LSParserFilter.FILTER_REJECT : org.w3c.dom.traversal.NodeFilter.FILTER_ACCEPT; }
            public int getWhatToShow() { return org.w3c.dom.traversal.NodeFilter.SHOW_ELEMENT; }
        };
        ser.setFilter(flt);
        check(ser.getFilter() == flt, "setFilter/getFilter round-trip");
        String out = ser.writeToString(d);
        check(!out.contains("<a"), "FILTER_REJECT prunes the element from the output");
        check(out.contains("<b/>"), "siblings survive");
        check(d.getDocumentElement().getChildNodes().getLength() == 2 && d.getDocumentElement().getFirstChild().getNodeName().equals("a"),
            "filtering mutates only the output, never the source tree");
        // version difference: JDK 27 keeps the rejected element's text, JDK 8 drops it with the parent
        boolean is8 = System.getProperty("java.version").startsWith("1.8");
        if (is8) check(!out.contains(">T"), "JDK 8: rejected element's text leaves the output with it");
        else check(out.contains(">T"), "JDK 27: rejected element's text content stays in the output");
        ser.setFilter(null);
        if (is8) check(ser.writeToString(d).contains("<a"), "JDK 8: clearing the filter restores full output");
        else check(!ser.writeToString(d).contains("<a"), "JDK 27: setFilter(null) keeps the previous filter (impl quirk)");
    }
}
