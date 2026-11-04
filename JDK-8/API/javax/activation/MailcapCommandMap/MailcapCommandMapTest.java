/*
 * @test
 * @summary javax.activation.MailcapCommandMap conformance: mailcap command map behavior.
 * @run main MailcapCommandMapTest
 */


public class MailcapCommandMapTest {
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
        javax.activation.MailcapCommandMap mcm = new javax.activation.MailcapCommandMap();
        check(mcm instanceof javax.activation.CommandMap, "extends CommandMap");
        check(mcm.getMimeTypes() != null, "mime types non-null");
        mcm.addMailcap("text/plain;; x-java-view=com.example.View");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
