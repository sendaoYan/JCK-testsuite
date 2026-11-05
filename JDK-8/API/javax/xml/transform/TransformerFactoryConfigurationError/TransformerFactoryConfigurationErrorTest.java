/*
 * @test
 * @summary javax.xml.transform.TransformerFactoryConfigurationError conformance: exception hierarchy and constructors.
 * @run main TransformerFactoryConfigurationErrorTest
 */


public class TransformerFactoryConfigurationErrorTest {
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
        check(javax.xml.transform.TransformerFactoryConfigurationError.class.getSuperclass().getName().equals("java.lang.Error"), "extends java.lang.Error");
        javax.xml.transform.TransformerFactoryConfigurationError fe = new javax.xml.transform.TransformerFactoryConfigurationError();
        check(fe.getException() == null, "no exception");
        testConstructorMatrix();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constructor matrix + real discovery failure
    static void testConstructorMatrix() {
        javax.xml.transform.TransformerFactoryConfigurationError e1 =
            new javax.xml.transform.TransformerFactoryConfigurationError("why");
        check("why".equals(e1.getMessage()), "(String) message");
        java.io.IOException cause = new java.io.IOException("io");
        javax.xml.transform.TransformerFactoryConfigurationError e2 =
            new javax.xml.transform.TransformerFactoryConfigurationError(cause);
        check(e2.getException() == cause && e2.getCause() == cause,
              "(Exception): getException and getCause are the exception");
        check("java.io.IOException: io".equals(e2.getMessage()),
              "(Exception): message defaults to exception toString");
        javax.xml.transform.TransformerFactoryConfigurationError e3 =
            new javax.xml.transform.TransformerFactoryConfigurationError(cause, "extra");
        check("extra".equals(e3.getMessage()) && e3.getException() == cause,
              "(Exception,String): custom message + exception");
        check(e1 instanceof java.lang.Error, "is java.lang.Error");
        javax.xml.transform.TransformerFactoryConfigurationError e0 =
            new javax.xml.transform.TransformerFactoryConfigurationError();
        check(e0.getMessage() == null && e0.getException() == null && e0.getCause() == null,
              "no-arg: all null");
    }

    static void testRealTrigger() {
        String key = "javax.xml.transform.TransformerFactory";
        String old = System.getProperty(key);
        System.setProperty(key, "no.such.TransformerFactory");
        try {
            boolean hit = false;
            try { javax.xml.transform.TransformerFactory.newInstance(); }
            catch (javax.xml.transform.TransformerFactoryConfigurationError err) {
                hit = err.getMessage() != null && err.getMessage().contains("no.such");
            }
            check(hit, "bogus factory system property -> TFCE naming the provider");
        } finally {
            if (old == null) System.clearProperty(key); else System.setProperty(key, old);
        }
        check(javax.xml.transform.TransformerFactory.newInstance() != null,
              "newInstance works again after property cleared");
    }
}
