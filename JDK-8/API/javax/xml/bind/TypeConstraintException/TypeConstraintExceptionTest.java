/*
 * @test
 * @summary javax.xml.bind.TypeConstraintException conformance: RuntimeException hierarchy (NOT JAXBException) and constructor matrix.
 * @run main TypeConstraintExceptionTest
 */


public class TypeConstraintExceptionTest {
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
        check(javax.xml.bind.TypeConstraintException.class.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends java.lang.RuntimeException");
        testHierarchy();
        testCtors();
        testNoCauseSync();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // --- key hierarchy fact: TypeConstraintException is NOT a JAXBException ---
    static void testHierarchy() throws Exception {
        check(!javax.xml.bind.JAXBException.class.isAssignableFrom(javax.xml.bind.TypeConstraintException.class),
                "NOT a javax.xml.bind.JAXBException (locked)");
        check(!javax.xml.bind.TypeConstraintException.class.isAssignableFrom(javax.xml.bind.JAXBException.class),
                "JAXBException is not a TypeConstraintException either");
        check(java.lang.RuntimeException.class.isAssignableFrom(javax.xml.bind.TypeConstraintException.class), "is a RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(javax.xml.bind.TypeConstraintException.class), "is Serializable");
        java.lang.reflect.Field f = javax.xml.bind.TypeConstraintException.class.getDeclaredField("serialVersionUID");
        f.setAccessible(true);
        check(f.getLong(null) == -3059799699420143848L, "declared serialVersionUID == -3059799699420143848");
        check(java.io.ObjectStreamClass.lookup(javax.xml.bind.TypeConstraintException.class).getSerialVersionUID() == -3059799699420143848L,
                "ObjectStreamClass serialVersionUID");
        check(javax.xml.bind.TypeConstraintException.class.getConstructors().length == 5, "5 public constructors");
    }

    static void testCtors() {
        javax.xml.bind.TypeConstraintException e1 = new javax.xml.bind.TypeConstraintException("m1");
        check("m1".equals(e1.getMessage()), "(String) message");
        check(e1.getErrorCode() == null, "(String) errorCode null");
        check(e1.getLinkedException() == null, "(String) linked null");
        check(e1.getCause() == null, "(String) cause null");

        javax.xml.bind.TypeConstraintException e2 = new javax.xml.bind.TypeConstraintException("m2", "code2");
        check("m2".equals(e2.getMessage()), "(String,String) message == first arg");
        check("code2".equals(e2.getErrorCode()), "(String,String) errorCode == second arg");

        RuntimeException t = new RuntimeException("boom");
        javax.xml.bind.TypeConstraintException e3 = new javax.xml.bind.TypeConstraintException(t);
        check(e3.getMessage() == null, "(Throwable) message null");
        check(e3.getLinkedException() == t, "(Throwable) linked == arg");
        check(e3.getCause() == null, "(Throwable) cause stays null (NO sync with linked, unlike JAXBException - locked)");

        javax.xml.bind.TypeConstraintException e4 = new javax.xml.bind.TypeConstraintException("m4", t);
        check("m4".equals(e4.getMessage()), "(String,Throwable) message");
        check(e4.getLinkedException() == t, "(String,Throwable) linked");
        check(e4.getCause() == null, "(String,Throwable) cause stays null (locked)");

        javax.xml.bind.TypeConstraintException e5 = new javax.xml.bind.TypeConstraintException("m5", "code5", t);
        check("m5".equals(e5.getMessage()), "(String,String,Throwable) message");
        check("code5".equals(e5.getErrorCode()), "(String,String,Throwable) errorCode");
        check(e5.getLinkedException() == t, "(String,String,Throwable) linked");
        check(e5.getCause() == null, "(String,String,Throwable) cause stays null (locked)");
    }

    // setLinkedException does not influence getCause (opposite of JAXBException semantics)
    static void testNoCauseSync() {
        javax.xml.bind.TypeConstraintException te = new javax.xml.bind.TypeConstraintException("msg", "code");
        check("code".equals(te.getErrorCode()), "error code");
        java.lang.Throwable le = new java.lang.Exception("linked");
        te.setLinkedException(le);
        check(te.getLinkedException() == le, "linked exception");
        check(te.getCause() == null, "getCause() remains null after setLinkedException (locked)");
        java.lang.Throwable le2 = new java.lang.Exception("second");
        te.setLinkedException(le2);
        check(te.getLinkedException() == le2, "setLinkedException replaceable");
        check(te.toString().contains("javax.xml.bind.TypeConstraintException"), "toString contains class name");
        check(te.toString().contains("with linked exception"), "toString marks linked exception");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new javax.xml.bind.TypeConstraintException("tp", le).printStackTrace(new java.io.PrintStream(bos));
        check(new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).contains("linked"), "printStackTrace includes linked");
        // unchecked channel: no throws clause needed
        boolean caught = false;
        try {
            throw new javax.xml.bind.TypeConstraintException("unchecked");
        } catch (RuntimeException rex) {
            caught = true;
            check(rex instanceof javax.xml.bind.TypeConstraintException, "catchable as RuntimeException");
        }
        check(caught, "throwable without declaration (unchecked)");
    }
}
