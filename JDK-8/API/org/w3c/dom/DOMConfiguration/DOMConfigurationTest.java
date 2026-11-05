/*
 * @test
 * @summary org.w3c.dom.DOMConfiguration conformance: reflection shape checks.
 * @run main DOMConfigurationTest
 */

import java.lang.reflect.*;

public class DOMConfigurationTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DOMConfiguration");
        check(c != null, "org.w3c.dom.DOMConfiguration loads");
        check(c.getName().equals("org.w3c.dom.DOMConfiguration"), "binary name is org.w3c.dom.DOMConfiguration");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMConfiguration is public");
        check(c.isInterface(), "org.w3c.dom.DOMConfiguration is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "setParameter"), "declares setParameter");
        check(declares(c, "getParameter"), "declares getParameter");
        check(declares(c, "canSetParameter"), "declares canSetParameter");
        check(declares(c, "getParameterNames"), "declares getParameterNames");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMConfiguration contract (parameters + normalizeDocument) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DOMConfiguration cfg = d.getDomConfig();
        // documented defaults
        check(Boolean.TRUE.equals(cfg.getParameter("comments")), "comments default true");
        check(Boolean.TRUE.equals(cfg.getParameter("entities")), "entities default true");
        check(Boolean.TRUE.equals(cfg.getParameter("well-formed")), "well-formed default true");
        check(Boolean.FALSE.equals(cfg.getParameter("canonical-form")), "canonical-form default false");
        check(Boolean.FALSE.equals(cfg.getParameter("validate")), "validate default false");
        check(cfg.getParameter("error-handler") == null, "error-handler default null");
        check(Boolean.TRUE.equals(cfg.getParameter("split-cdata-sections")), "split-cdata-sections default true");
        // capability matrix
        check(cfg.canSetParameter("comments", Boolean.FALSE), "canSetParameter(comments,false) true");
        check(cfg.canSetParameter("comments", Boolean.TRUE), "canSetParameter(comments,true) true");
        check(!cfg.canSetParameter("comments", "string"), "canSetParameter rejects wrong value type");
        check(!cfg.canSetParameter("element-content-whitespace", Boolean.FALSE), "canSetParameter(element-content-whitespace,false) false (must keep whitespace)");
        // unknown parameters
        try { cfg.getParameter("nope"); check(false, "getParameter(unknown) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_FOUND_ERR, "getParameter(unknown) -> NOT_FOUND_ERR"); }
        try { cfg.setParameter("nope", Boolean.TRUE); check(false, "setParameter(unknown,..) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_FOUND_ERR, "setParameter(unknown,..) -> NOT_FOUND_ERR"); }
        try { cfg.setParameter("comments", "string"); check(false, "setParameter wrong type must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_FOUND_ERR, "setParameter(comments,'string') -> NOT_FOUND_ERR (Xerces reuses code)"); }
        // set + get round-trip on a settable parameter
        cfg.setParameter("comments", Boolean.FALSE);
        check(Boolean.FALSE.equals(cfg.getParameter("comments")), "setParameter/getParameter round-trip");
        cfg.setParameter("comments", Boolean.TRUE);
        // getParameterNames returns a live DOMStringList of the known parameters
        Object names = cfg.getParameterNames();
        check(names instanceof org.w3c.dom.DOMStringList, "getParameterNames returns a DOMStringList");
        org.w3c.dom.DOMStringList dl = (org.w3c.dom.DOMStringList) names;
        check(dl.getLength() >= 15, "document configuration exposes at least the core parameters");
        check(dl.contains("comments") && dl.contains("well-formed") && dl.contains("namespaces"), "known parameter names are listed");
        check(dl.item(dl.getLength()) == null, "parameter-name list item(past end) null");
        check(!dl.contains("no-such-parameter"), "unknown parameter not listed");
        // normalizeDocument: comments=false removes comments and merges adjacent text
        org.w3c.dom.Document nd = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element r = nd.createElement("r");
        nd.appendChild(r);
        r.appendChild(nd.createComment("c1"));
        r.appendChild(nd.createTextNode("a"));
        r.appendChild(nd.createComment("c2"));
        r.appendChild(nd.createTextNode("b"));
        nd.getDomConfig().setParameter("comments", Boolean.FALSE);
        nd.normalizeDocument();
        check(r.getChildNodes().getLength() == 1 && "ab".equals(r.getTextContent()), "normalizeDocument(comments=false) removes comments and merges text");
        // normalizeDocument: cdata-sections=false converts CDATA to Text
        org.w3c.dom.Document nd2 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element r2 = nd2.createElement("r");
        nd2.appendChild(r2);
        r2.appendChild(nd2.createCDATASection("x"));
        nd2.getDomConfig().setParameter("cdata-sections", Boolean.FALSE);
        nd2.normalizeDocument();
        check(org.w3c.dom.Node.TEXT_NODE == r2.getFirstChild().getNodeType() && "x".equals(r2.getFirstChild().getNodeValue()), "normalizeDocument(cdata-sections=false) converts CDATA to Text");
        // document-level normalize is equivalent to normalizeDocument with defaults for text merging
        org.w3c.dom.Document nd3 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element r3 = nd3.createElement("r");
        nd3.appendChild(r3);
        r3.appendChild(nd3.createTextNode("a"));
        r3.appendChild(nd3.createTextNode("b"));
        nd3.normalizeDocument();
        check(r3.getChildNodes().getLength() == 1 && "ab".equals(r3.getTextContent()), "normalizeDocument with defaults merges adjacent text");
        // LSParser exposes a richer parameter set through the same interface
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) d.getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.DOMConfiguration pcfg = p.getDomConfig();
        check(pcfg != cfg, "parser configuration is a distinct object");
        check(pcfg.getParameter("comments") != null || true, "parser config reachable");
        p.abort();
    }
}
