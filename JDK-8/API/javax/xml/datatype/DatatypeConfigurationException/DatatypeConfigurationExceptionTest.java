/*
 * @test
 * @summary javax.xml.datatype.DatatypeConfigurationException conformance: exception hierarchy and constructors.
 * @run main DatatypeConfigurationExceptionTest
 */


public class DatatypeConfigurationExceptionTest {
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
        check(javax.xml.datatype.DatatypeConfigurationException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        testConstructorChains();
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- constructor chain matrix ----
    static void testConstructorChains() {
        javax.xml.datatype.DatatypeConfigurationException e1 = new javax.xml.datatype.DatatypeConfigurationException("boom");
        check("boom".equals(e1.getMessage()), "(String) message");
        check(e1.getCause() == null, "(String) no cause");
        RuntimeException root = new RuntimeException("root");
        javax.xml.datatype.DatatypeConfigurationException e2 = new javax.xml.datatype.DatatypeConfigurationException("boom", root);
        check("boom".equals(e2.getMessage()), "(String,Throwable) message");
        check(e2.getCause() == root, "(String,Throwable) cause identity");
        javax.xml.datatype.DatatypeConfigurationException e3 = new javax.xml.datatype.DatatypeConfigurationException(root);
        check(e3.getCause() == root, "(Throwable) cause identity");
        check(e3.getMessage() != null && e3.getMessage().contains("root"),
              "(Throwable) message derived from cause toString");
        javax.xml.datatype.DatatypeConfigurationException e4 = new javax.xml.datatype.DatatypeConfigurationException();
        check(e4.getMessage() == null && e4.getCause() == null, "() null message and cause");
        check(e1 instanceof java.lang.Exception, "is java.lang.Exception");
        check(e1 instanceof java.io.Serializable, "Serializable via Exception");
    }

    // ---- white-box: shape + constructor inventory ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.datatype.DatatypeConfigurationException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass Exception");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 4, "four constructors");
        int stringOnly = 0, stringThrowable = 0, throwableOnly = 0, noArg = 0;
        for (java.lang.reflect.Constructor<?> ct : ctors) {
            Class<?>[] ps = ct.getParameterTypes();
            if (ps.length == 1 && ps[0] == String.class) stringOnly++;
            else if (ps.length == 2 && ps[0] == String.class && Throwable.class.isAssignableFrom(ps[1])) stringThrowable++;
            else if (ps.length == 1 && Throwable.class.isAssignableFrom(ps[0])) throwableOnly++;
            else if (ps.length == 0) noArg++;
        }
        check(stringOnly == 1, "one (String) ctor");
        check(stringThrowable == 1, "one (String,Throwable) ctor");
        check(throwableOnly == 1, "one (Throwable) ctor");
        check(noArg == 1, "one () ctor");
    }
}
