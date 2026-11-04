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

        // ---- JDK-19+ factory methods ----
        check(TransformerFactory.newDefaultInstance() != null, "newDefaultInstance non-null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
