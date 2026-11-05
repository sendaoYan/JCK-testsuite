/*
 * @test
 * @summary javax.xml.transform.ErrorListener conformance: interface hierarchy and methods.
 * @run main ErrorListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ErrorListenerTest {
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
        Class<?> c = Class.forName("javax.xml.transform.ErrorListener");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("warning"), "has method warning");
        check(names.contains("error"), "has method error");
        check(names.contains("fatalError"), "has method fatalError");
        testCompileErrorCallbacks();
        testRuntimeCallbacks();
        testRethrowPropagation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: real warning/error/fatalError trigger closures
    static final String DOC2 =
        "<root><item id=\"3\" name=\"gamma\">third</item></root>";

    static void testCompileErrorCallbacks() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        javax.xml.transform.ErrorListener lst = new javax.xml.transform.ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) { calls.add("W:" + e.getMessage()); }
            public void error(javax.xml.transform.TransformerException e) { calls.add("E:" + e.getMessage()); }
            public void fatalError(javax.xml.transform.TransformerException e) { calls.add("F:" + e.getMessage()); }
        };
        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        f.setErrorListener(lst);
        boolean tce = false;
        try {
            f.newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:template match=\"/\"><unclosed></xsl:stylesheet>")));
        } catch (javax.xml.transform.TransformerConfigurationException e) { tce = true; }
        check(tce, "compile error still throws TCE when listener swallows");
        boolean sawError = false, sawFatal = false;
        for (String c : calls) {
            if (c.startsWith("E:")) sawError = true;
            if (c.startsWith("F:") && c.contains("unclosed")) sawFatal = true;
        }
        check(sawError, "factory compile error reaches ErrorListener.error");
        check(sawFatal, "factory compile error reaches ErrorListener.fatalError naming element");
        check(f.getErrorListener() == lst, "setErrorListener/getErrorListener round-trip");
        // invalid XPath: recorded message contains the offending expression text
        final java.util.List<String> xp = new java.util.ArrayList<String>();
        javax.xml.transform.TransformerFactory f2 = javax.xml.transform.TransformerFactory.newInstance();
        f2.setErrorListener(new javax.xml.transform.ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) { }
            public void error(javax.xml.transform.TransformerException e) { xp.add(e.getMessage()); }
            public void fatalError(javax.xml.transform.TransformerException e) { xp.add(e.getMessage()); }
        });
        try {
            f2.newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:template match=\"/\"><xsl:value-of select=\"!!!\"/></xsl:template></xsl:stylesheet>")));
        } catch (javax.xml.transform.TransformerException e) { xp.add(e.getMessage()); }
        boolean containsExpr = false;
        for (String m : xp) if (m.contains("!!!")) containsExpr = true;
        check(containsExpr, "invalid XPath '!!!' reported through listener with original text");
    }

    static void testRuntimeCallbacks() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        javax.xml.transform.ErrorListener lst = new javax.xml.transform.ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) { calls.add("W:" + e.getMessage()); }
            public void error(javax.xml.transform.TransformerException e) { calls.add("E:" + e.getMessage()); }
            public void fatalError(javax.xml.transform.TransformerException e) { calls.add("F:" + e.getMessage()); }
        };
        // xsl:message terminate=yes: warning carries the message text, error reports termination,
        // and transform throws TransformerException
        javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance()
            .newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:template match=\"/\"><xsl:message terminate=\"yes\">boom</xsl:message></xsl:template>"
              + "</xsl:stylesheet>")));
        t.setErrorListener(lst);
        boolean te = false;
        try {
            t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC2)),
                        new javax.xml.transform.stream.StreamResult(new java.io.StringWriter()));
        } catch (javax.xml.transform.TransformerException e) {
            te = e.getMessage() != null && e.getMessage().contains("xsl:message");
        }
        check(te, "xsl:message terminate=yes -> TransformerException mentioning xsl:message");
        boolean warnBoom = false;
        for (String c : calls) if (c.startsWith("W:") && c.contains("boom")) warnBoom = true;
        check(warnBoom, "xsl:message text delivered to ErrorListener.warning");
        check(t.getErrorListener() == lst, "transformer setErrorListener/getErrorListener round-trip");
        // runtime error: invalid computed QName reaches error()
        final java.util.List<String> rc = new java.util.ArrayList<String>();
        javax.xml.transform.Transformer t2 = javax.xml.transform.TransformerFactory.newInstance()
            .newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:template match=\"/\"><xsl:element name=\"{root/item[1]/@name}\"/></xsl:template>"
              + "</xsl:stylesheet>")));
        t2.setErrorListener(new javax.xml.transform.ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) { }
            public void error(javax.xml.transform.TransformerException e) { rc.add(e.getMessage()); }
            public void fatalError(javax.xml.transform.TransformerException e) { rc.add(e.getMessage()); }
        });
        boolean te2 = false;
        try {
            t2.transform(new javax.xml.transform.stream.StreamSource(
                    new java.io.StringReader("<root><item id=\"1\" name=\"1gamma\"/></root>")),
                new javax.xml.transform.stream.StreamResult(new java.io.StringWriter()));
        } catch (javax.xml.transform.TransformerException e) { te2 = true; }
        check(te2, "runtime invalid QName -> TransformerException");
        boolean saw1gamma = false;
        for (String m : rc) if (m.contains("1gamma")) saw1gamma = true;
        check(saw1gamma, "runtime error reported through error() naming the bad value");
    }

    static void testRethrowPropagation() throws Exception {
        // an ErrorListener that throws propagates its own exception out of newTransformer
        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        f.setErrorListener(new javax.xml.transform.ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) { }
            public void error(javax.xml.transform.TransformerException e) { }
            public void fatalError(javax.xml.transform.TransformerException e) {
                throw new RuntimeException("listener-boom");
            }
        });
        boolean propagated = false;
        try {
            f.newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:template match=\"/\"><unclosed></xsl:stylesheet>")));
        } catch (RuntimeException e) {
            propagated = "listener-boom".equals(e.getMessage());
        }
        check(propagated, "listener-thrown RuntimeException propagates out of newTransformer");
    }
}
