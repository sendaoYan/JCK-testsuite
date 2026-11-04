/*
 * @test
 * @summary javax.xml.bind.helpers.DefaultValidationEventHandler conformance: default validation event handler.
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
