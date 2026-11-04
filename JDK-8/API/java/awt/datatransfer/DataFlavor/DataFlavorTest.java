/*
 * @test
 * @summary java.awt.datatransfer.DataFlavor conformance: mime type parsing.
 * @run main DataFlavorTest
 */


import java.awt.datatransfer.DataFlavor;
public class DataFlavorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        DataFlavor f = new DataFlavor("text/plain; charset=UTF-8", "Plain Text");
        check(f.getMimeType().startsWith("text/plain"), "mime type");
        check(f.getPrimaryType().equals("text"), "primary type");
        check(f.getSubType().equals("plain"), "sub type");
        check(f.getParameter("charset").equals("UTF-8"), "parameter");
        check(f.getHumanPresentableName().equals("Plain Text"), "human name");
        check(f.isFlavorTextType(), "is text type");
        check(f.isMimeTypeEqual("text/plain"), "mime equal");
        check(f.match(f), "match self");
        check(DataFlavor.stringFlavor.isFlavorTextType(), "stringFlavor is text type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
