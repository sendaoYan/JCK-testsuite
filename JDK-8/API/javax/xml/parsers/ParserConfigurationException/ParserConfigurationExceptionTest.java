/*
 * @test
 * @summary javax.xml.parsers.ParserConfigurationException conformance: exception hierarchy and constructors.
 * @run main ParserConfigurationExceptionTest
 */


public class ParserConfigurationExceptionTest {
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
        check(javax.xml.parsers.ParserConfigurationException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");

        // ---- constructor matrix ----
        javax.xml.parsers.ParserConfigurationException e0 = new javax.xml.parsers.ParserConfigurationException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getCause() == null, "no-arg cause null");
        javax.xml.parsers.ParserConfigurationException e1 = new javax.xml.parsers.ParserConfigurationException("msg");
        check("msg".equals(e1.getMessage()), "(String) message preserved");
        check(e1.getCause() == null, "(String) cause null");
        javax.xml.parsers.ParserConfigurationException eNull = new javax.xml.parsers.ParserConfigurationException(null);
        check(eNull.getMessage() == null, "null message allowed");

        // ---- API surface: exactly () and (String) constructors (no cause ctor) ----
        check(javax.xml.parsers.ParserConfigurationException.class.getConstructors().length == 2,
              "exactly two public constructors");

        // ---- serializable checked exception semantics ----
        check(java.io.Serializable.class.isAssignableFrom(javax.xml.parsers.ParserConfigurationException.class),
              "PCE is serializable (Exception)");
        // thrown by DocumentBuilderFactory when a requested feature cannot be provided
        boolean sawPce = false;
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            check(db != null, "newDocumentBuilder succeeds for validating config");
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            sawPce = true;
        }
        check(!sawPce, "standard validating configuration is supported");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
