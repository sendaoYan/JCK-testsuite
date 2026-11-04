/*
 * @test
 * @summary javax.xml.transform.Transformer conformance: identity transform round-trip, parameters, output properties, null/exception semantics, reset.
 * @run main TransformerTest
 */

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

public class TransformerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

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
    static boolean isTransformerException(Thrower t) {
        try { t.run(); return false; }
        catch (TransformerException e) { return true; }
        catch (Exception e) { return false; }
    }

    static final String XML = "<root><a x=\"1\">text</a></root>";

    public static void main(String[] args) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        // silence default error listener noise
        final ErrorListener quiet = new ErrorListener() {
            public void warning(TransformerException e) { }
            public void error(TransformerException e) { }
            public void fatalError(TransformerException e) { }
        };
        tf.setErrorListener(quiet);
        Transformer tr = tf.newTransformer();
        tr.setErrorListener(quiet);

        // ---- identity transform round-trip (StreamSource -> StreamResult) ----
        StringWriter sw = new StringWriter();
        tr.transform(new StreamSource(new StringReader(XML)), new StreamResult(sw));
        String out = sw.toString();
        check(out.contains("<?xml version=\"1.0\""), "identity transform emits xml declaration");
        check(out.contains("<root>"), "identity transform preserves root");
        check(out.contains("<a x=\"1\">text</a>") || out.contains("<a x=\"1\">"), "identity transform preserves element/attr/text");

        // ---- null source -> TransformerException ----
        check(isTransformerException(() -> tr.transform(null, new StreamResult(new StringWriter()))),
              "transform(null source) -> TransformerException");

        // ---- parameters ----
        tr.setParameter("p1", "v1");
        check("v1".equals(tr.getParameter("p1")), "setParameter/getParameter");
        check(tr.getParameter("missing") == null, "getParameter(missing) null");
        check(isIae(() -> tr.setParameter("p1", null)), "setParameter(name, null) -> IAE");
        tr.setParameter("p2", "v2");
        tr.clearParameters();
        check(tr.getParameter("p2") == null, "clearParameters clears all");

        // ---- output properties ----
        check("xml".equals(tr.getOutputProperty(OutputKeys.METHOD)), "default output method xml");
        tr.setOutputProperty(OutputKeys.METHOD, "text");
        check("text".equals(tr.getOutputProperty(OutputKeys.METHOD)), "setOutputProperty/getOutputProperty");
        Properties props = new Properties();
        props.setProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperties(props);
        check("xml".equals(tr.getOutputProperties().getProperty(OutputKeys.METHOD)), "setOutputProperties round-trip");
        check(isNpe(() -> tr.setOutputProperty(null, "v")), "setOutputProperty(null name) -> NPE");
        check(isNpe(() -> tr.getOutputProperty(null)), "getOutputProperty(null name) -> NPE");

        // ---- listeners / resolver ----
        check(isIae(() -> tr.setErrorListener(null)), "setErrorListener(null) -> IAE");
        tr.setErrorListener(quiet);
        check(tr.getErrorListener() == quiet, "setErrorListener/getErrorListener round-trip");
        URIResolver ur = new URIResolver() {
            public javax.xml.transform.Source resolve(String href, String base) { return null; }
        };
        tr.setURIResolver(ur);
        check(tr.getURIResolver() == ur, "setURIResolver/getURIResolver round-trip");

        // ---- reset keeps transformer usable ----
        tr.reset();
        StringWriter sw2 = new StringWriter();
        tr.transform(new StreamSource(new StringReader(XML)), new StreamResult(sw2));
        check(sw2.toString().contains("<root>"), "transformer usable after reset");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
