/*
 * @test
 * @summary javax.xml.bind.helpers.ValidationEventImpl conformance: constructor/mutator matrix, severity domain and toString.
 * @run main ValidationEventImplTest
 */


public class ValidationEventImplTest {
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
        javax.xml.bind.helpers.ValidationEventImpl e = new javax.xml.bind.helpers.ValidationEventImpl(2, "msg", (javax.xml.bind.ValidationEventLocator) null);
        check(e.getSeverity() == 2, "severity");
        check("msg".equals(e.getMessage()), "message");
        e.setSeverity(1);
        check(e.getSeverity() == 1, "set severity");
        e.setMessage("x");
        check("x".equals(e.getMessage()), "set message");
        java.lang.Throwable t = new java.lang.Exception("cause");
        e.setLinkedException(t);
        check(e.getLinkedException() == t, "linked exception");
        check(e.getLocator() == null, "locator null");
        testCtorMatrix();
        testSeverityDomain();
        testMutators();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testCtorMatrix() {
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        loc.setLineNumber(2);
        javax.xml.bind.helpers.ValidationEventImpl e =
                new javax.xml.bind.helpers.ValidationEventImpl(0, "m", loc);
        check(e.getSeverity() == 0, "3-arg ctor severity");
        check("m".equals(e.getMessage()), "3-arg ctor message");
        check(e.getLocator() == loc, "3-arg ctor locator identity");
        check(e.getLinkedException() == null, "3-arg ctor linked null");

        RuntimeException t = new RuntimeException("inner");
        javax.xml.bind.helpers.ValidationEventImpl e4 =
                new javax.xml.bind.helpers.ValidationEventImpl(2, "m4", loc, t);
        check(e4.getSeverity() == 2, "4-arg ctor severity");
        check("m4".equals(e4.getMessage()), "4-arg ctor message");
        check(e4.getLocator() == loc, "4-arg ctor locator identity");
        check(e4.getLinkedException() == t, "4-arg ctor linked exception");

        // null message / null locator are permitted
        javax.xml.bind.helpers.ValidationEventImpl en =
                new javax.xml.bind.helpers.ValidationEventImpl(1, null, null);
        check(en.getMessage() == null, "null message allowed");
        check(en.getLocator() == null, "null locator allowed");
        check(en instanceof javax.xml.bind.ValidationEvent, "implements ValidationEvent");
        check(javax.xml.bind.helpers.ValidationEventImpl.class.getConstructors().length == 2, "2 public constructors");
    }

    // severity outside [0,2] is rejected both at construction and mutation (locked)
    static void testSeverityDomain() {
        boolean i1 = false;
        try { new javax.xml.bind.helpers.ValidationEventImpl(-1, "m", null); }
        catch (IllegalArgumentException e) { i1 = true; }
        check(i1, "ctor severity -1 throws IllegalArgumentException (locked)");
        boolean i2 = false;
        try { new javax.xml.bind.helpers.ValidationEventImpl(3, "m", null); }
        catch (IllegalArgumentException e) { i2 = true; }
        check(i2, "ctor severity 3 throws IllegalArgumentException (locked)");
        javax.xml.bind.helpers.ValidationEventImpl e =
                new javax.xml.bind.helpers.ValidationEventImpl(0, "m", null);
        boolean i3 = false;
        try { e.setSeverity(-1); } catch (IllegalArgumentException x) { i3 = true; }
        check(i3, "setSeverity(-1) throws IllegalArgumentException (locked)");
        boolean i4 = false;
        try { e.setSeverity(5); } catch (IllegalArgumentException x) { i4 = true; }
        check(i4, "setSeverity(5) throws IllegalArgumentException (locked)");
        check(e.getSeverity() == 0, "severity unchanged after failed set");
        // every legal value round trips
        e.setSeverity(1);
        check(e.getSeverity() == 1, "setSeverity(ERROR) ok");
        e.setSeverity(2);
        check(e.getSeverity() == 2, "setSeverity(FATAL_ERROR) ok");
        e.setSeverity(0);
        check(e.getSeverity() == 0, "setSeverity(WARNING) ok");
    }

    static void testMutators() {
        javax.xml.bind.helpers.ValidationEventImpl e =
                new javax.xml.bind.helpers.ValidationEventImpl(0, "a", null);
        e.setMessage(null);
        check(e.getMessage() == null, "setMessage(null) allowed");
        e.setMessage("b");
        check("b".equals(e.getMessage()), "setMessage readback");
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc2 =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        e.setLocator(loc2);
        check(e.getLocator() == loc2, "setLocator readback");
        e.setLocator(null);
        check(e.getLocator() == null, "setLocator(null) allowed");
        RuntimeException t1 = new RuntimeException("1");
        RuntimeException t2 = new RuntimeException("2");
        e.setLinkedException(t1);
        check(e.getLinkedException() == t1, "setLinkedException readback");
        e.setLinkedException(t2);
        check(e.getLinkedException() == t2, "setLinkedException replaceable");
        e.setLinkedException(null);
        check(e.getLinkedException() == null, "setLinkedException(null) allowed");
    }

    // toString locks the human-readable form incl. severity names
    static void testToString() {
        check(new javax.xml.bind.helpers.ValidationEventImpl(0, "w", null).toString().contains("severity=WARNING"),
                "toString severity name WARNING");
        check(new javax.xml.bind.helpers.ValidationEventImpl(1, "e", null).toString().contains("severity=ERROR"),
                "toString severity name ERROR");
        javax.xml.bind.helpers.ValidationEventImpl f =
                new javax.xml.bind.helpers.ValidationEventImpl(2, "f", null);
        check(f.toString().contains("severity=FATAL_ERROR"), "toString severity name FATAL_ERROR");
        check(f.toString().contains("message=f"), "toString contains message");
        check(f.toString().contains("locator=null"), "toString shows locator=null");
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        loc.setLineNumber(6);
        javax.xml.bind.helpers.ValidationEventImpl withLoc =
                new javax.xml.bind.helpers.ValidationEventImpl(1, "L", loc);
        check(withLoc.toString().contains("line=6"), "toString includes locator line");
    }
}
