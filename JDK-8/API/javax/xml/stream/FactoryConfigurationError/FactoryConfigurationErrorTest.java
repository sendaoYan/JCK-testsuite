/*
 * @test
 * @summary javax.xml.stream.FactoryConfigurationError conformance: exception hierarchy and constructors.
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
        check(javax.xml.stream.FactoryConfigurationError.class.getSuperclass().getName().equals("java.lang.Error"), "extends java.lang.Error");
        javax.xml.stream.FactoryConfigurationError fe = new javax.xml.stream.FactoryConfigurationError();
        check(fe.getException() == null, "no exception");
        testConstructorChain();
        testThrowability();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: all four constructor overloads ----
    static void testConstructorChain() {
        javax.xml.stream.FactoryConfigurationError e1 = new javax.xml.stream.FactoryConfigurationError("why1");
        check("why1".equals(e1.getMessage()), "(String) message");
        check(e1.getException() == null && e1.getCause() == null, "(String) no exception/cause");
        java.lang.Exception io = new java.io.IOException("ioe");
        javax.xml.stream.FactoryConfigurationError e2 = new javax.xml.stream.FactoryConfigurationError(io);
        check(e2.getException() == io, "(Exception) getException identity");
        check(e2.getCause() == io, "(Exception) getCause identity");
        check("ioe".equals(e2.getMessage()), "(Exception) message from exception");
        javax.xml.stream.FactoryConfigurationError e3 = new javax.xml.stream.FactoryConfigurationError(io, "extra");
        check("extra".equals(e3.getMessage()), "(Exception,String) message wins");
        check(e3.getException() == io, "(Exception,String) exception kept");
        javax.xml.stream.FactoryConfigurationError e4 = new javax.xml.stream.FactoryConfigurationError();
        check(e4.getMessage() == null && e4.getException() == null, "no-arg all null");
        // hierarchy and throwability
        check(e4 instanceof java.lang.Error, "is Error");
        check(e4 instanceof java.io.Serializable, "Error is Serializable");
    }

    // ---- Deepened 2: thrown and caught like an Error; factory discovery can raise it ----
    static void testThrowability() {
        boolean caught = false;
        try {
            throw new javax.xml.stream.FactoryConfigurationError("probe");
        } catch (java.lang.Error e) {
            caught = e instanceof javax.xml.stream.FactoryConfigurationError
                && "probe".equals(e.getMessage());
        }
        check(caught, "throwable/catchable as Error with message");
        // invalid factory class via system property -> FactoryConfigurationError, property restored in finally
        String key = "javax.xml.stream.XMLInputFactory";
        String saved = System.getProperty(key);
        try {
            System.setProperty(key, "no.such.Clazz");
            boolean fce = false;
            try {
                javax.xml.stream.XMLInputFactory.newFactory();
            } catch (javax.xml.stream.FactoryConfigurationError e) {
                fce = "no.such.Clazz".equals(String.valueOf(e.getMessage()))
                    || String.valueOf(e.getMessage()).contains("no.such.Clazz")
                    || e.getCause() != null || e.getException() != null;
            }
            check(fce, "unresolvable factory class -> FactoryConfigurationError");
        } finally {
            if (saved == null) System.clearProperty(key); else System.setProperty(key, saved);
        }
        check(System.getProperty(key) == null ? saved == null : saved.equals(System.getProperty(key)),
            "factory property restored after error path");
    }
}
