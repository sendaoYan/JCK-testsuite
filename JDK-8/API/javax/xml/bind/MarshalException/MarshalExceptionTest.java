/*
 * @test
 * @summary javax.xml.bind.MarshalException conformance: hierarchy, constructors, error code and linked cause sync.
 * @run main MarshalExceptionTest
 */


public class MarshalExceptionTest {
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
        check(javax.xml.bind.MarshalException.class.getSuperclass().getName().equals("javax.xml.bind.JAXBException"), "extends javax.xml.bind.JAXBException");
        testHierarchy();
        testCtors();
        testSync();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testHierarchy() throws Exception {
        check(javax.xml.bind.JAXBException.class.isAssignableFrom(javax.xml.bind.MarshalException.class), "is a JAXBException");
        check(java.lang.Exception.class.isAssignableFrom(javax.xml.bind.MarshalException.class), "is an Exception");
        check(java.io.Serializable.class.isAssignableFrom(javax.xml.bind.MarshalException.class), "is Serializable");
        // serialVersionUID computed by ObjectStreamClass, locked by probe on JDK 8 RI
        check(java.io.ObjectStreamClass.lookup(javax.xml.bind.MarshalException.class).getSerialVersionUID() == -6039934860308470324L,
                "serialVersionUID == -6039934860308470324");
        check(javax.xml.bind.MarshalException.class.getConstructors().length == 5, "5 public constructors");
    }

    static void testCtors() {
        javax.xml.bind.MarshalException e1 = new javax.xml.bind.MarshalException("m1");
        check("m1".equals(e1.getMessage()), "(String) message");
        check(e1.getErrorCode() == null, "(String) errorCode null");
        check(e1.getLinkedException() == null, "(String) linked null");
        check(e1.getCause() == null, "(String) cause null");

        javax.xml.bind.MarshalException e2 = new javax.xml.bind.MarshalException("m2", "code2");
        check("m2".equals(e2.getMessage()), "(String,String) message == first arg");
        check("code2".equals(e2.getErrorCode()), "(String,String) errorCode == second arg");

        RuntimeException t = new RuntimeException("boom");
        javax.xml.bind.MarshalException e3 = new javax.xml.bind.MarshalException(t);
        check(e3.getMessage() == null, "(Throwable) message null");
        check(e3.getLinkedException() == t, "(Throwable) linked == arg");
        check(e3.getCause() == t, "(Throwable) cause == arg");

        javax.xml.bind.MarshalException e4 = new javax.xml.bind.MarshalException("m4", t);
        check("m4".equals(e4.getMessage()), "(String,Throwable) message");
        check(e4.getLinkedException() == t, "(String,Throwable) linked");
        check(e4.getCause() == t, "(String,Throwable) cause");

        javax.xml.bind.MarshalException e5 = new javax.xml.bind.MarshalException("m5", "code5", t);
        check("m5".equals(e5.getMessage()), "(String,String,Throwable) message");
        check("code5".equals(e5.getErrorCode()), "(String,String,Throwable) errorCode");
        check(e5.getLinkedException() == t, "(String,String,Throwable) linked");
        check(e5.getCause() == t, "(String,String,Throwable) cause");
    }

    static void testSync() {
        RuntimeException t = new RuntimeException("a");
        javax.xml.bind.MarshalException e = new javax.xml.bind.MarshalException("s", t);
        RuntimeException t2 = new RuntimeException("b");
        e.setLinkedException(t2);
        check(e.getLinkedException() == t2, "setLinkedException readback");
        check(e.getCause() == t2, "getCause() follows setLinkedException (sync)");
        check(e.toString().contains("javax.xml.bind.MarshalException"), "toString contains class name");
        check(e.toString().contains("with linked exception"), "toString marks linked exception");
        // catch as supertype and use JAXBException-typed API
        try {
            throw new javax.xml.bind.MarshalException("poly", "ec");
        } catch (javax.xml.bind.JAXBException ex) {
            check("ec".equals(ex.getErrorCode()), "catchable as JAXBException with errorCode intact");
            check(ex instanceof javax.xml.bind.MarshalException, "dynamic type preserved");
        }
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new javax.xml.bind.MarshalException("mp", new RuntimeException("mc")).printStackTrace(new java.io.PrintStream(bos));
        check(new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).contains("mc"), "printStackTrace includes linked exception");
    }
}
