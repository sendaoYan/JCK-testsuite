/*
 * @test
 * @summary javax.activation.ActivationDataFlavor conformance: mime type flavor behavior.
 * @run main ActivationDataFlavorTest
 */


public class ActivationDataFlavorTest {
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
        javax.activation.ActivationDataFlavor f = new javax.activation.ActivationDataFlavor("text/plain", "desc");
        check("text/plain".equals(f.getMimeType()), "mime type");
        check(f instanceof java.awt.datatransfer.DataFlavor, "extends DataFlavor");
        check(f.isMimeTypeEqual("text/plain"), "mime type equal");
        f.setHumanPresentableName("plain text");
        check("plain text".equals(f.getHumanPresentableName()), "human name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
