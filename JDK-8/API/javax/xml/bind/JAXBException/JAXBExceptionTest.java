/*
 * @test
 * @summary javax.xml.bind.JAXBException conformance: constructors, error code, linked exception and cause synchronization.
 * @run main JAXBExceptionTest
 */


public class JAXBExceptionTest {
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
        check(javax.xml.bind.JAXBException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        javax.xml.bind.JAXBException je = new javax.xml.bind.JAXBException("msg", "code");
        check("code".equals(je.getErrorCode()), "error code");
        java.lang.Throwable le = new java.lang.Exception("linked");
        je.setLinkedException(le);
        check(je.getLinkedException() == le, "linked exception");
        testSerial();
        testCtors();
        testCauseSync();
        testToStringPrint();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // --- serialization contract: declared serialVersionUID locked by probe ---
    static void testSerial() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(javax.xml.bind.JAXBException.class), "implements Serializable");
        java.lang.reflect.Field f = javax.xml.bind.JAXBException.class.getDeclaredField("serialVersionUID");
        f.setAccessible(true);
        check(f.getLong(null) == -5621384651494307979L, "declared serialVersionUID == -5621384651494307979");
        check(java.io.ObjectStreamClass.lookup(javax.xml.bind.JAXBException.class).getSerialVersionUID() == -5621384651494307979L,
                "ObjectStreamClass serialVersionUID");
        check(javax.xml.bind.JAXBException.class.getConstructors().length == 5, "5 public constructors (String)/(String,String)/(Throwable)/(String,Throwable)/(String,String,Throwable)");
    }

    // --- full constructor matrix ---
    static void testCtors() {
        javax.xml.bind.JAXBException e1 = new javax.xml.bind.JAXBException("m1");
        check("m1".equals(e1.getMessage()), "(String) message");
        check(e1.getErrorCode() == null, "(String) errorCode null");
        check(e1.getLinkedException() == null, "(String) linked null");
        check(e1.getCause() == null, "(String) cause null");

        javax.xml.bind.JAXBException e2 = new javax.xml.bind.JAXBException("m2", "code2");
        check("m2".equals(e2.getMessage()), "(String,String) message == first arg");
        check("code2".equals(e2.getErrorCode()), "(String,String) errorCode == second arg");
        check(e2.getLinkedException() == null, "(String,String) linked null");
        check(e2.getCause() == null, "(String,String) cause null");

        RuntimeException t = new RuntimeException("boom");
        javax.xml.bind.JAXBException e3 = new javax.xml.bind.JAXBException(t);
        check(e3.getMessage() == null, "(Throwable) message null (RI)");
        check(e3.getLinkedException() == t, "(Throwable) linked == arg");
        check(e3.getCause() == t, "(Throwable) cause == arg");

        javax.xml.bind.JAXBException e4 = new javax.xml.bind.JAXBException("m4", t);
        check("m4".equals(e4.getMessage()), "(String,Throwable) message");
        check(e4.getLinkedException() == t, "(String,Throwable) linked");
        check(e4.getCause() == t, "(String,Throwable) cause");

        javax.xml.bind.JAXBException e5 = new javax.xml.bind.JAXBException("m5", "code5", t);
        check("m5".equals(e5.getMessage()), "(String,String,Throwable) message");
        check("code5".equals(e5.getErrorCode()), "(String,String,Throwable) errorCode");
        check(e5.getLinkedException() == t, "(String,String,Throwable) linked");
        check(e5.getCause() == t, "(String,String,Throwable) cause");

        javax.xml.bind.JAXBException e6 = new javax.xml.bind.JAXBException("m6", "");
        check("".equals(e6.getErrorCode()), "empty errorCode allowed and read back");

        // polymorphic catch: JAXBException caught as Exception, nested readback intact
        try {
            throw new javax.xml.bind.JAXBException("caught", "c");
        } catch (Exception ex) {
            check(ex instanceof javax.xml.bind.JAXBException, "thrown as JAXBException catchable as Exception");
            check("c".equals(((javax.xml.bind.JAXBException) ex).getErrorCode()), "errorCode survives polymorphic catch");
        }
    }

    // --- getCause()/getLinkedException() synchronization (RI overrides getCause) ---
    static void testCauseSync() {
        RuntimeException t = new RuntimeException("first");
        javax.xml.bind.JAXBException e = new javax.xml.bind.JAXBException("sync", t);
        check(e.getCause() == e.getLinkedException(), "getCause() mirrors getLinkedException() after (String,Throwable)");
        RuntimeException t2 = new RuntimeException("second");
        e.setLinkedException(t2);
        check(e.getLinkedException() == t2, "setLinkedException readback");
        check(e.getCause() == t2, "setLinkedException updates getCause() too (synchronized)");

        // (Throwable)-form: initCause does not throw ISE (underlying cause was never initialized)
        javax.xml.bind.JAXBException e3 = new javax.xml.bind.JAXBException(t);
        boolean noIse = true;
        try { e3.initCause(t2); } catch (IllegalStateException ise) { noIse = false; }
        check(noIse, "initCause after (Throwable) ctor does not throw IllegalStateException");
        check(e3.getCause() == t, "getCause() still returns original linked exception");
        check(e3.getLinkedException() == t, "getLinkedException() unchanged by initCause");

        // (String)-form: initCause works at Throwable level but the overridden accessors stay null (RI-locked)
        javax.xml.bind.JAXBException e1 = new javax.xml.bind.JAXBException("solo");
        boolean ok = true;
        try { ok = (e1.initCause(t2) == e1); } catch (IllegalStateException ise) { ok = false; }
        check(ok, "initCause on (String)-form returns this without throwing");
        check(e1.getLinkedException() == null, "getLinkedException() still null after initCause (RI override)");
        check(e1.getCause() == null, "getCause() still null after initCause (RI override)");
    }

    // --- toString / printStackTrace forms ---
    static void testToStringPrint() {
        javax.xml.bind.JAXBException plain = new javax.xml.bind.JAXBException("onlyMsg");
        check(plain.toString().contains("javax.xml.bind.JAXBException"), "toString contains class name");
        check(plain.toString().contains("onlyMsg"), "toString contains message");

        javax.xml.bind.JAXBException withLinked = new javax.xml.bind.JAXBException("withMsg", new RuntimeException("inner"));
        check(withLinked.toString().contains("with linked exception"), "toString marks linked exception");
        check(withLinked.toString().contains("inner"), "toString contains linked exception text");
        check(withLinked.toString().contains("withMsg"), "toString keeps message with linked present");

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.bind.JAXBException pe = new javax.xml.bind.JAXBException("pp", new RuntimeException("qq"));
        pe.printStackTrace(new java.io.PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) produces output");
        String s = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(s.contains("pp"), "stack trace contains message");
        check(s.contains("qq"), "stack trace contains linked exception");
        pe.printStackTrace(); // PrintStream default, must not throw
        pe.printStackTrace(new java.io.PrintWriter(new java.io.StringWriter()));
        check(true, "printStackTrace()/printStackTrace(PrintWriter) do not throw");
    }
}
