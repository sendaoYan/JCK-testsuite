/*
 * @test
 * @summary javax.xml.transform.URIResolver conformance: white-box structure + black-box behavior.
 * @run main URIResolverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class URIResolverTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.transform.URIResolver");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "resolve"), "method resolve");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.transform.URIResolver a = new javax.xml.transform.URIResolver(){
            public javax.xml.transform.Source resolve(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of URIResolver");
        check(a.resolve("s", "s") == null, "resolve() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
