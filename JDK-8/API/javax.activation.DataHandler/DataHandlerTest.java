/*
 * @test
 * @summary javax.activation.DataHandler conformance: data handler behavior.
 * @run main DataHandlerTest
 */


public class DataHandlerTest {
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
        javax.activation.DataHandler dh = new javax.activation.DataHandler("hello", "text/plain");
        check("text/plain".equals(dh.getContentType()), "content type");
        check("hello".equals(dh.getContent()), "content");
        check(dh instanceof java.awt.datatransfer.Transferable, "is Transferable");
        check(dh.getTransferDataFlavors() != null, "flavors non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
