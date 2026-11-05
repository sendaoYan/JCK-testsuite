/*
 * @test
 * @summary javax.xml.transform.TransformerConfigurationException conformance: exception hierarchy and constructors.
 * @run main TransformerConfigurationExceptionTest
 */


public class TransformerConfigurationExceptionTest {
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
        check(javax.xml.transform.TransformerConfigurationException.class.getSuperclass().getName().equals("javax.xml.transform.TransformerException"), "extends javax.xml.transform.TransformerException");
        testConstructorChains();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: every constructor + real trigger
    static final javax.xml.transform.SourceLocator LOC = new javax.xml.transform.SourceLocator() {
        public String getPublicId() { return "pub"; }
        public String getSystemId() { return "sys"; }
        public int getLineNumber() { return 1; }
        public int getColumnNumber() { return 2; }
    };

    static void testConstructorChains() {
        javax.xml.transform.TransformerConfigurationException e0 =
            new javax.xml.transform.TransformerConfigurationException();
        check("Configuration Error".equals(e0.getMessage()), "no-arg default message 'Configuration Error'");
        check(e0.getException() == null && e0.getLocator() == null, "no-arg: no wrapped exception/locator");
        javax.xml.transform.TransformerConfigurationException e1 =
            new javax.xml.transform.TransformerConfigurationException("c1");
        check("c1".equals(e1.getMessage()), "(String) message");
        javax.xml.transform.TransformerConfigurationException e2 =
            new javax.xml.transform.TransformerConfigurationException("c2", LOC);
        check("c2".equals(e2.getMessage()) && e2.getLocator() == LOC,
              "(String,SourceLocator): locator kept");
        RuntimeException r = new RuntimeException("rc");
        javax.xml.transform.TransformerConfigurationException e3 =
            new javax.xml.transform.TransformerConfigurationException("c3", r);
        check("c3".equals(e3.getMessage()) && e3.getException() == r && e3.getCause() == r,
              "(String,Exception): message + wrapped exception as cause");
        javax.xml.transform.TransformerConfigurationException e4 =
            new javax.xml.transform.TransformerConfigurationException(r);
        check("java.lang.RuntimeException: rc".equals(e4.getMessage()),
              "(Exception): message defaults to exception toString");
        check(e4.getException() == r, "(Exception): getException set");
        javax.xml.transform.TransformerConfigurationException e5 =
            new javax.xml.transform.TransformerConfigurationException("c5", LOC, r);
        check(e5.getLocator() == LOC && e5.getCause() == r,
              "(String,SourceLocator,Exception): all three kept");
        check(e5 instanceof javax.xml.transform.TransformerException,
              "ConfigurationException is a TransformerException");
        check(e5.getMessageAndLocation() != null, "inherited getMessageAndLocation works");
    }

    static void testRealTrigger() throws Exception {
        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        f.setErrorListener(new javax.xml.transform.ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) { }
            public void error(javax.xml.transform.TransformerException e) { }
            public void fatalError(javax.xml.transform.TransformerException e) { }
        });
        boolean tce = false;
        try {
            f.newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:template match=\"/\"><unclosed></xsl:stylesheet>")));
        } catch (javax.xml.transform.TransformerConfigurationException e) {
            tce = e.getMessage() != null && e.getMessage().contains("unclosed");
        }
        check(tce, "unclosed tag in stylesheet -> TransformerConfigurationException naming element");
        boolean tce2 = false;
        try {
            f.newTemplates(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<not-a-stylesheet/>")));
        } catch (javax.xml.transform.TransformerConfigurationException e) {
            tce2 = e.getMessage() != null;
        }
        check(tce2, "non-stylesheet source -> TransformerConfigurationException with message");
    }
}
