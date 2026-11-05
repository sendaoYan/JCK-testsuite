/*
 * @test
 * @summary javax.xml.xpath.XPathFactoryConfigurationException conformance: exception hierarchy and constructors.
 * @run main XPathFactoryConfigurationExceptionTest
 */


public class XPathFactoryConfigurationExceptionTest {
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
        check(javax.xml.xpath.XPathFactoryConfigurationException.class.getSuperclass().getName().equals("javax.xml.xpath.XPathException"), "extends javax.xml.xpath.XPathException");
        testConstructors();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static void testConstructors() {
        // (String)
        javax.xml.xpath.XPathFactoryConfigurationException e1 =
            new javax.xml.xpath.XPathFactoryConfigurationException("no impl");
        check("no impl".equals(e1.getMessage()), "(String) message round-trip");
        check(e1.getCause() == null, "(String) cause null");
        // (Throwable)
        RuntimeException cause = new RuntimeException("cfg root");
        javax.xml.xpath.XPathFactoryConfigurationException e2 =
            new javax.xml.xpath.XPathFactoryConfigurationException(cause);
        check(e2.getCause() == cause, "(Throwable) cause round-trip");
        check(e2.getMessage() != null && e2.getMessage().contains("cfg root"), "(Throwable) message derived");
        // two constructors only
        check(javax.xml.xpath.XPathFactoryConfigurationException.class.getConstructors().length == 2,
              "exactly two public constructors");
        // it is a checked XPathException subtype
        check(e2 instanceof javax.xml.xpath.XPathException, "is-a XPathException");
    }

    // real trigger: unsupported object model
    static void testRealTrigger() throws Exception {
        boolean saw = false;
        try {
            javax.xml.xpath.XPathFactory.newInstance("http://bogus.object.model");
        } catch (javax.xml.xpath.XPathFactoryConfigurationException e) {
            saw = e.getMessage() != null && e.getMessage().contains("http://bogus.object.model");
        }
        check(saw, "newInstance(bogus object model) real trigger naming the model");
    }
}
