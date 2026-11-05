/*
 * @test
 * @summary javax.xml.bind.helpers.DefaultValidationEventHandler conformance: severity verdict matrix and event reporting.
 * @run main DefaultValidationEventHandlerTest
 */


public class DefaultValidationEventHandlerTest {
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
        javax.xml.bind.helpers.ValidationEventImpl ev = new javax.xml.bind.helpers.ValidationEventImpl(1, "msg", (javax.xml.bind.ValidationEventLocator) null);
        javax.xml.bind.helpers.DefaultValidationEventHandler h = new javax.xml.bind.helpers.DefaultValidationEventHandler();
        check(!h.handleEvent(ev), "handleEvent returns false");
        testVerdictMatrix();
        testInterface();
        testNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // core contract: WARNING continues (true), ERROR and FATAL_ERROR abort (false)
    static void testVerdictMatrix() {
        javax.xml.bind.helpers.DefaultValidationEventHandler h = new javax.xml.bind.helpers.DefaultValidationEventHandler();
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc = new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        loc.setLineNumber(4);
        loc.setColumnNumber(9);

        javax.xml.bind.ValidationEvent warn =
                new javax.xml.bind.helpers.ValidationEventImpl(javax.xml.bind.ValidationEvent.WARNING, "w", loc);
        javax.xml.bind.ValidationEvent err =
                new javax.xml.bind.helpers.ValidationEventImpl(javax.xml.bind.ValidationEvent.ERROR, "e", loc);
        javax.xml.bind.ValidationEvent fatal =
                new javax.xml.bind.helpers.ValidationEventImpl(javax.xml.bind.ValidationEvent.FATAL_ERROR, "f", loc);
        check(h.handleEvent(warn) == true, "handleEvent(WARNING) == true (locked)");
        check(h.handleEvent(err) == false, "handleEvent(ERROR) == false (locked)");
        check(h.handleEvent(fatal) == false, "handleEvent(FATAL_ERROR) == false (locked)");

        // verdicts are stable across repetitions and independent of payload
        check(h.handleEvent(warn) == true, "WARNING verdict stable on repeat");
        check(h.handleEvent(err) == false, "ERROR verdict stable on repeat");
        javax.xml.bind.ValidationEvent err2 =
                new javax.xml.bind.helpers.ValidationEventImpl(javax.xml.bind.ValidationEvent.ERROR, "other", null);
        check(h.handleEvent(err2) == false, "ERROR verdict independent of message/locator");

        // events carrying a linked exception keep the same verdict
        javax.xml.bind.ValidationEvent fatalLinked =
                new javax.xml.bind.helpers.ValidationEventImpl(
                        javax.xml.bind.ValidationEvent.FATAL_ERROR, "fl", loc, new RuntimeException("inner"));
        check(h.handleEvent(fatalLinked) == false, "FATAL with linked exception still false");
        javax.xml.bind.ValidationEvent warnLinked =
                new javax.xml.bind.helpers.ValidationEventImpl(
                        javax.xml.bind.ValidationEvent.WARNING, "wl", loc, new RuntimeException("inner"));
        check(h.handleEvent(warnLinked) == true, "WARNING with linked exception still true");

        // fresh instance behaves identically (stateless handler)
        javax.xml.bind.helpers.DefaultValidationEventHandler h2 = new javax.xml.bind.helpers.DefaultValidationEventHandler();
        check(h2.handleEvent(warn) && !h2.handleEvent(err), "fresh instance same verdict matrix");
    }

    static void testInterface() throws Exception {
        javax.xml.bind.helpers.DefaultValidationEventHandler h = new javax.xml.bind.helpers.DefaultValidationEventHandler();
        check(h instanceof javax.xml.bind.ValidationEventHandler, "implements ValidationEventHandler");
        check(javax.xml.bind.ValidationEventHandler.class.isAssignableFrom(javax.xml.bind.helpers.DefaultValidationEventHandler.class),
                "assignable to handler interface");
        check(javax.xml.bind.helpers.DefaultValidationEventHandler.class.getConstructors().length == 1,
                "single public no-arg constructor");
    }

    static void testNegative() {
        javax.xml.bind.helpers.DefaultValidationEventHandler h = new javax.xml.bind.helpers.DefaultValidationEventHandler();
        boolean iae = false;
        try {
            h.handleEvent(null);
        } catch (IllegalArgumentException e) {
            iae = true;
        }
        check(iae, "handleEvent(null) throws IllegalArgumentException (locked)");
    }
}
