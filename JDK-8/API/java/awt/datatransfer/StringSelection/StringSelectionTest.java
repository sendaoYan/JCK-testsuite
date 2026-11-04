/*
 * @test
 * @summary java.awt.datatransfer.StringSelection conformance: transferable string.
 * @run main StringSelectionTest
 */


import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
public class StringSelectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        StringSelection s = new StringSelection("hello");
        DataFlavor[] flavors = s.getTransferDataFlavors();
        check(flavors.length == 2, "two flavors");
        check(flavors[0].equals(DataFlavor.stringFlavor), "first flavor is stringFlavor");
        check(s.isDataFlavorSupported(DataFlavor.stringFlavor), "stringFlavor supported");
        check(!s.isDataFlavorSupported(DataFlavor.javaFileListFlavor), "file list not supported");
        Object data = s.getTransferData(DataFlavor.stringFlavor);
        check("hello".equals(data), "transfer data");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
