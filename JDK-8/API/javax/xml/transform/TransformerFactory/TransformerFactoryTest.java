/*
 * @test
 * @summary javax.xml.transform.TransformerFactory conformance: factory boundary, precise exceptions, null semantics, transformer/templates production.
 * @run main TransformerFactoryTest
 */

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import java.io.StringReader;

public class TransformerFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isFce(Thrower t) {
        try { t.run(); return false; }
        catch (TransformerFactoryConfigurationError e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isTce(Thrower t) {
        try { t.run(); return false; }
        catch (TransformerConfigurationException e) { return true; }
        catch (Exception e) { return false; }
    }

    static final String XSL =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
        "<xsl:template match=\"/\"><xsl:copy-of select=\".\"/></xsl:template></xsl:stylesheet>";

    public static void main(String[] args) throws Exception {
        // ---- factory boundary ----
        TransformerFactory f = TransformerFactory.newInstance();
        check(f != null, "newInstance non-null");
        ClassLoader cl = TransformerFactory.class.getClassLoader();
        TransformerFactory f2 = TransformerFactory.newInstance(
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", cl);
        check(f2 != null, "newInstance(className, loader) non-null");

        // ---- precise factory exceptions ----
        check(isFce(() -> TransformerFactory.newInstance("no.such.Factory", cl)),
              "bogus impl class -> TransformerFactoryConfigurationError");
        check(isFce(() -> TransformerFactory.newInstance(null, null)),
              "null impl class -> TransformerFactoryConfigurationError");

        // ---- null semantics ----
        check(isNpe(() -> f.setAttribute(null, "v")), "setAttribute(null,..) -> NPE");
        check(isNpe(() -> f.getAttribute(null)), "getAttribute(null) -> NPE");
        check(isIae(() -> f.setErrorListener(null)), "setErrorListener(null) -> IAE");

        // ---- feature ----
        check(isTce(() -> f.setFeature("http://no.such/feature", true)), "setFeature(unknown) -> TransformerConfigurationException");
        check(!f.getFeature("http://no.such/feature"), "getFeature(unknown) -> false");

        // ---- listeners / resolver round-trip ----
        final boolean[] warned = {false};
        ErrorListener el = new ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) { warned[0] = true; }
            public void error(javax.xml.transform.TransformerException e) { }
            public void fatalError(javax.xml.transform.TransformerException e) { }
        };
        f.setErrorListener(el);
        check(f.getErrorListener() == el, "setErrorListener/getErrorListener round-trip");
        URIResolver ur = new URIResolver() {
            public javax.xml.transform.Source resolve(String href, String base) { return null; }
        };
        f.setURIResolver(ur);
        check(f.getURIResolver() == ur, "setURIResolver/getURIResolver round-trip");

        // ---- transformer production ----
        Transformer tr = f.newTransformer();
        check(tr != null, "newTransformer non-null");
        Transformer tr2 = f.newTransformer();
        check(tr2 != null && tr2 != tr, "newTransformer returns fresh transformer");

        // ---- templates production ----
        Templates templates = f.newTemplates(new StreamSource(new StringReader(XSL)));
        check(templates != null, "newTemplates non-null");
        Transformer tr3 = templates.newTransformer();
        check(tr3 != null, "Templates.newTransformer non-null");
        // non-stylesheet source -> TransformerConfigurationException
        check(isTce(() -> f.newTemplates(new StreamSource(new StringReader("<not-a-stylesheet/>")))),
              "newTemplates(non-stylesheet) -> TransformerConfigurationException");

        testFeatureMatrix();
        testAttributeSemantics();
        testUriResolverAdoption();
        testAssociatedStylesheet();
        testDiscoveryFailure();
        testCompileErrors();
        testJdk8Surface();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== deepening =====================

    static final String DOC3 =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static String norm(String s) { return s.replace("\r", ""); }

    static void testFeatureMatrix() throws Exception {
        TransformerFactory f = TransformerFactory.newInstance();
        check(f.getFeature(javax.xml.transform.dom.DOMSource.FEATURE), "getFeature(DOMSource.FEATURE)=true");
        check(f.getFeature(javax.xml.transform.dom.DOMResult.FEATURE), "getFeature(DOMResult.FEATURE)=true");
        check(f.getFeature(javax.xml.transform.sax.SAXSource.FEATURE), "getFeature(SAXSource.FEATURE)=true");
        check(f.getFeature(javax.xml.transform.sax.SAXResult.FEATURE), "getFeature(SAXResult.FEATURE)=true");
        check(f.getFeature(javax.xml.transform.stax.StAXSource.FEATURE), "getFeature(StAXSource.FEATURE)=true");
        check(f.getFeature(javax.xml.transform.stax.StAXResult.FEATURE), "getFeature(StAXResult.FEATURE)=true");
        check(f.getFeature(javax.xml.transform.sax.SAXTransformerFactory.FEATURE),
              "getFeature(SAXTransformerFactory.FEATURE)=true");
        check(f.getFeature(javax.xml.transform.sax.SAXTransformerFactory.FEATURE_XMLFILTER),
              "getFeature(FEATURE_XMLFILTER)=true");
        check("http://javax.xml.transform.sax.SAXTransformerFactory/feature"
                  .equals(javax.xml.transform.sax.SAXTransformerFactory.FEATURE),
              "SAXTransformerFactory.FEATURE constant value");
        check("http://javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter"
                  .equals(javax.xml.transform.sax.SAXTransformerFactory.FEATURE_XMLFILTER),
              "SAXTransformerFactory.FEATURE_XMLFILTER constant value");
    }

    static void testAttributeSemantics() throws Exception {
        TransformerFactory f = TransformerFactory.newInstance();
        f.setAttribute("translet-name", "MyTranslet");
        check("MyTranslet".equals(f.getAttribute("translet-name")),
              "setAttribute/getAttribute round-trip (translet-name)");
        boolean iaeSet = false;
        try { f.setAttribute("no.such.attr", "x"); }
        catch (IllegalArgumentException e) { iaeSet = e.getMessage() != null && e.getMessage().contains("no.such.attr"); }
        check(iaeSet, "setAttribute(unknown) -> IAE naming the attribute");
        boolean iaeGet = false;
        try { f.getAttribute("no.such.attr"); }
        catch (IllegalArgumentException e) { iaeGet = true; }
        check(iaeGet, "getAttribute(unknown) -> IAE");
    }

    // xsl:include resolved through a custom URIResolver whose replacement Source is adopted
    static void testUriResolverAdoption() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        TransformerFactory f = TransformerFactory.newInstance();
        f.setURIResolver(new URIResolver() {
            public javax.xml.transform.Source resolve(String href, String base) {
                calls.add(href + "|" + base);
                if ("inc.xsl".equals(href)) {
                    return new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                      + "<xsl:template match=\"item\">"
                      + "<got id=\"{@id}\"><xsl:value-of select=\".\"/></got>"
                      + "</xsl:template></xsl:stylesheet>"));
                }
                return null;
            }
        });
        String mainXsl =
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
          + "<xsl:include href=\"inc.xsl\"/>"
          + "<xsl:template match=\"/\"><inc><xsl:apply-templates select=\"root/item[1]\"/></inc></xsl:template>"
          + "</xsl:stylesheet>";
        Transformer t = f.newTransformer(new javax.xml.transform.stream.StreamSource(
            new java.io.StringReader(mainXsl), "http://base/main.xsl"));
        check(calls.contains("inc.xsl|http://base/main.xsl"),
              "URIResolver.resolve called with (href, base)");
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC3)),
                    new javax.xml.transform.stream.StreamResult(sw));
        check(norm(sw.toString()).equals("<inc><got id=\"3\">third</got></inc>"),
              "resolver-provided replacement Source adopted (include closure)");
    }

    static void testAssociatedStylesheet() throws Exception {
        TransformerFactory f = TransformerFactory.newInstance();
        String docWithPi =
            "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?><root><a/></root>";
        javax.xml.transform.Source asc = f.getAssociatedStylesheet(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(docWithPi)),
            null, null, null);
        check(asc != null, "getAssociatedStylesheet finds xml-stylesheet PI");
        check(asc.getSystemId() != null && asc.getSystemId().endsWith("style.xsl"),
              "associated stylesheet href resolved to systemId");
        javax.xml.transform.Source none = f.getAssociatedStylesheet(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<root/>")),
            null, null, null);
        check(none == null, "no xml-stylesheet PI -> null");
    }

    static void testDiscoveryFailure() throws Exception {
        String key = "javax.xml.transform.TransformerFactory";
        String old = System.getProperty(key);
        System.setProperty(key, "no.such.TransformerFactory");
        try {
            boolean tfce = false;
            try { TransformerFactory.newInstance(); }
            catch (TransformerFactoryConfigurationError e) {
                tfce = e.getMessage() != null && e.getMessage().contains("no.such");
            }
            check(tfce, "bogus system property -> TransformerFactoryConfigurationError naming provider");
        } finally {
            if (old == null) System.clearProperty(key); else System.setProperty(key, old);
        }
        check(TransformerFactory.newInstance() != null,
              "newInstance recovers after system property cleared");
    }

    static void testCompileErrors() throws Exception {
        TransformerFactory f = TransformerFactory.newInstance();
        f.setErrorListener(new ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) { }
            public void error(javax.xml.transform.TransformerException e) { }
            public void fatalError(javax.xml.transform.TransformerException e) { }
        });
        boolean tce = false;
        try {
            f.newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:template match=\"/\"><unclosed></xsl:stylesheet>")));
        } catch (TransformerConfigurationException e) {
            tce = e.getMessage() != null && e.getMessage().contains("unclosed");
        }
        check(tce, "XSLT syntax error (unclosed tag) -> TCE naming the element");
        check(isNpe(() -> f.newTransformer(null)), "newTransformer(null) -> NPE");
        check(isNpe(() -> f.newTemplates(null)), "newTemplates(null) -> NPE");
    }

    static void testJdk8Surface() throws Exception {
        // JDK-8 lacks TransformerFactory.newDefaultInstance() (added later); lock absence.
        boolean absent = false;
        try { TransformerFactory.class.getMethod("newDefaultInstance"); }
        catch (NoSuchMethodException e) { absent = true; }
        check(absent, "JDK-8: TransformerFactory.newDefaultInstance absent");
    }
}
