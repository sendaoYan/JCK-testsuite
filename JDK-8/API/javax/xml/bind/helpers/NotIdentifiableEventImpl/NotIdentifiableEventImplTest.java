/*
 * @test
 * @summary javax.xml.bind.helpers.NotIdentifiableEventImpl conformance: not identifiable event impl.
 * @run main NotIdentifiableEventImplTest
 */


public class NotIdentifiableEventImplTest {
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
        javax.xml.bind.helpers.NotIdentifiableEventImpl e = new javax.xml.bind.helpers.NotIdentifiableEventImpl(1, "msg", (javax.xml.bind.ValidationEventLocator) null);
        check(e.getSeverity() == 1, "severity");
        check("msg".equals(e.getMessage()), "message");
        check(e instanceof javax.xml.bind.NotIdentifiableEvent, "is NotIdentifiableEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
