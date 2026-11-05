/*
 * @test
 * @summary javax.xml.bind.DataBindingException conformance: RuntimeException form, two constructors, no JAXBException relation.
 * @run main DataBindingExceptionTest
 */


public class DataBindingExceptionTest {
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
        check(javax.xml.bind.DataBindingException.class.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends java.lang.RuntimeException");
        testHierarchy();
        testCtors();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testHierarchy() throws Exception {
        check(!javax.xml.bind.JAXBException.class.isAssignableFrom(javax.xml.bind.DataBindingException.class),
                "NOT a JAXBException (locked)");
        check(javax.xml.bind.DataBindingException.class.isAssignableFrom(javax.xml.bind.JAXBException.class) == false,
                "no relation in the other direction either");
        check(java.lang.RuntimeException.class.isAssignableFrom(javax.xml.bind.DataBindingException.class), "is a RuntimeException");
        check(java.lang.Exception.class.isAssignableFrom(javax.xml.bind.DataBindingException.class), "is an Exception");
        check(java.io.Serializable.class.isAssignableFrom(javax.xml.bind.DataBindingException.class), "is Serializable");
        check(java.io.ObjectStreamClass.lookup(javax.xml.bind.DataBindingException.class).getSerialVersionUID() == -3559750092881944571L,
                "serialVersionUID == -3559750092881944571");
        check(javax.xml.bind.DataBindingException.class.getConstructors().length == 2, "exactly 2 public constructors");
        // no (String) single-arg constructor
        boolean noStringCtor = false;
        try {
            javax.xml.bind.DataBindingException.class.getConstructor(String.class);
        } catch (NoSuchMethodException nsme) {
            noStringCtor = true;
        }
        check(noStringCtor, "no (String)-only constructor");
        // JAXBException-typed API is absent on this type
        boolean noErrorCode = false;
        try {
            javax.xml.bind.DataBindingException.class.getMethod("getErrorCode");
        } catch (NoSuchMethodException nsme) {
            noErrorCode = true;
        }
        check(noErrorCode, "no getErrorCode() method (unlike JAXBException)");
        boolean noLinked = false;
        try {
            javax.xml.bind.DataBindingException.class.getMethod("setLinkedException", java.lang.Throwable.class);
        } catch (NoSuchMethodException nsme) {
            noLinked = true;
        }
        check(noLinked, "no setLinkedException() method");
    }

    static void testCtors() {
        RuntimeException t = new RuntimeException("boom");
        javax.xml.bind.DataBindingException d1 = new javax.xml.bind.DataBindingException(t);
        check("java.lang.RuntimeException: boom".equals(d1.getMessage()), "(Throwable) message == throwable.toString() (locked)");
        check(d1.getCause() == t, "(Throwable) cause == arg");
        check(d1.toString().contains("javax.xml.bind.DataBindingException"), "(Throwable) toString contains class name");

        javax.xml.bind.DataBindingException d2 = new javax.xml.bind.DataBindingException("dbm", t);
        check("dbm".equals(d2.getMessage()), "(String,Throwable) message == first arg");
        check(d2.getCause() == t, "(String,Throwable) cause == second arg");
        check(d2.toString().contains("dbm"), "(String,Throwable) toString contains message");

        javax.xml.bind.DataBindingException d3 = new javax.xml.bind.DataBindingException("solo", null);
        check("solo".equals(d3.getMessage()), "null cause allowed in (String,Throwable)");
        check(d3.getCause() == null, "null cause read back as null");

        javax.xml.bind.DataBindingException d4 = new javax.xml.bind.DataBindingException((Throwable) null);
        check(d4.getMessage() == null, "(null Throwable) message null");
        check(d4.getCause() == null, "(null Throwable) cause null");

        // unchecked channel
        boolean caught = false;
        try {
            throw new javax.xml.bind.DataBindingException("x", t);
        } catch (RuntimeException rex) {
            caught = true;
            check(rex.getCause() == t, "cause survives unchecked catch");
        }
        check(caught, "throwable without declaration (unchecked)");
    }
}
