/*
 * @test
 * @summary javax.xml.transform.URIResolver conformance: interface hierarchy and methods.
 * @run main URIResolverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class URIResolverTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.transform.URIResolver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("resolve"), "has method resolve");
        testIncludeAdoption();
        testDocumentAdoption();
        testNullFallsBack();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: resolver adoption closures
    static final String DOC2 =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item></root>";

    static String norm(String s) { return s.replace("\r", ""); }

    static String run(javax.xml.transform.TransformerFactory f, String xsl, String xml) throws Exception {
        javax.xml.transform.Transformer t = f.newTransformer(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(xsl), "http://base/main.xsl"));
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(xml)),
                    new javax.xml.transform.stream.StreamResult(sw));
        return norm(sw.toString());
    }

    static void testIncludeAdoption() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        f.setURIResolver(new javax.xml.transform.URIResolver() {
            public javax.xml.transform.Source resolve(String href, String base) {
                calls.add(href + "|" + base);
                if ("inc.xsl".equals(href)) {
                    return new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                      + "<xsl:template match=\"item\"><got id=\"{@id}\"><xsl:value-of select=\".\"/></got>"
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
        String out = run(f, mainXsl, DOC2);
        check(calls.contains("inc.xsl|http://base/main.xsl"),
              "xsl:include triggers resolve(href='inc.xsl', base=stylesheet systemId)");
        check(out.equals("<inc><got id=\"3\">third</got></inc>"),
              "replacement Source returned by resolver is adopted (output exact)");
        javax.xml.transform.URIResolver cur = f.getURIResolver();
        check(cur != null, "factory getURIResolver returns the installed resolver");
    }

    static void testDocumentAdoption() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        String docXsl =
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
          + "<xsl:template match=\"/\"><doc><xsl:value-of select=\"document('data.xml')/root/x\"/></doc>"
          + "</xsl:template></xsl:stylesheet>";
        javax.xml.transform.Transformer t = f.newTransformer(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(docXsl)));
        t.setURIResolver(new javax.xml.transform.URIResolver() {
            public javax.xml.transform.Source resolve(String href, String base) {
                calls.add(href + "|" + base);
                if ("data.xml".equals(href)) {
                    return new javax.xml.transform.stream.StreamSource(
                        new java.io.StringReader("<root><x>docval</x></root>"));
                }
                return null;
            }
        });
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC2)),
                    new javax.xml.transform.stream.StreamResult(sw));
        check(calls.contains("data.xml|"), "document() triggers resolve(href='data.xml')");
        check(norm(sw.toString()).equals("<doc>docval</doc>"),
              "resolver-supplied document feeds document() (output exact)");
        check(t.getURIResolver() != null, "transformer getURIResolver returns installed resolver");
    }

    static void testNullFallsBack() throws Exception {
        // resolve() returning null -> implementation default resolution (file base fails fast)
        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        f.setURIResolver(new javax.xml.transform.URIResolver() {
            public javax.xml.transform.Source resolve(String href, String base) { return null; }
        });
        boolean tce = false;
        try {
            f.newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:include href=\"inc.xsl\"/>"
              + "<xsl:template match=\"/\"><z/></xsl:template></xsl:stylesheet>"),
                "file:///nonexistent-dir-xyz/main.xsl"));
        } catch (javax.xml.transform.TransformerConfigurationException e) {
            tce = e.getMessage() != null;
        }
        check(tce, "resolve()=null falls back to default resolution -> TCE for missing file");
    }
}
