/*
 * @test
 * @summary javax.xml.parsers.FactoryConfigurationError conformance: exception hierarchy and constructors.
 * @run main FactoryConfigurationErrorTest
 */


public class FactoryConfigurationErrorTest {
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
        check(javax.xml.parsers.FactoryConfigurationError.class.getSuperclass().getName().equals("java.lang.Error"), "extends java.lang.Error");
        javax.xml.parsers.FactoryConfigurationError fe = new javax.xml.parsers.FactoryConfigurationError();
        check(fe.getException() == null, "no exception");

        // ---- constructor matrix ----
        IllegalStateException cause = new IllegalStateException("boom");
        javax.xml.parsers.FactoryConfigurationError e1 = new javax.xml.parsers.FactoryConfigurationError(cause);
        check(e1.getException() == cause, "(Exception) getException identity");
        check(e1.getCause() == cause, "(Exception) getCause identity");
        check(e1.getMessage() != null && e1.getMessage().contains("boom"),
              "(Exception) message defaults to cause toString");
        javax.xml.parsers.FactoryConfigurationError e2 = new javax.xml.parsers.FactoryConfigurationError(cause, "msg");
        check("msg".equals(e2.getMessage()), "(Exception,String) message wins");
        check(e2.getException() == cause && e2.getCause() == cause, "(Exception,String) exception/cause");
        javax.xml.parsers.FactoryConfigurationError e3 = new javax.xml.parsers.FactoryConfigurationError("plain");
        check("plain".equals(e3.getMessage()), "(String) message");
        check(e3.getException() == null, "(String) no exception");
        javax.xml.parsers.FactoryConfigurationError e4 = new javax.xml.parsers.FactoryConfigurationError();
        check(e4.getMessage() == null && e4.getException() == null, "() both null");

        // ---- discovery failure surfaces this error ----
        boolean sawError = false;
        try {
            javax.xml.parsers.DocumentBuilderFactory.newInstance("no.such.Factory", null);
        } catch (javax.xml.parsers.FactoryConfigurationError e) {
            sawError = e.getMessage() != null && e.getMessage().contains("no.such.Factory");
        }
        check(sawError, "bogus factory class -> FactoryConfigurationError naming the class");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
