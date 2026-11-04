/*
 * @test
 * @summary javax.xml.bind.util.ValidationEventCollector conformance: validation event collector.
 * @run main ValidationEventCollectorTest
 */


public class ValidationEventCollectorTest {
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
        javax.xml.bind.util.ValidationEventCollector c = new javax.xml.bind.util.ValidationEventCollector();
        check(!c.hasEvents(), "no events");
        check(c.getEvents().length == 0, "empty events");
        javax.xml.bind.helpers.ValidationEventImpl ev = new javax.xml.bind.helpers.ValidationEventImpl(1, "msg", (javax.xml.bind.ValidationEventLocator) null);
        check(c.handleEvent(ev), "handleEvent true");
        check(c.hasEvents(), "has events");
        c.reset();
        check(!c.hasEvents(), "reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
